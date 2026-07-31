# 🔍 业务功能问题与优化审计

> 本章节基于对现有业务代码的静态审计，记录**真实存在的问题**（缺陷 / 安全漏洞 / 逻辑错误 / 性能与资源问题 / 错误处理缺陷）及优化点。
> 优先级：🔴 高危（安全或功能性阻断）｜🟠 中（功能受损 / 明显隐患）｜🟡 低（健壮性 / 体验 / 优化）。
> 前提：项目已配置全局登录拦截器（`WebMvcConfig` 的 `SaTokenInterceptor`，仅放行 login/captcha/dashboard/chat.stream 等少数路径），故所有 `/ai/**` 端点默认需登录；下文“缺认证”类问题多指缺少**角色校验 / 资源归属校验**，而非完全无认证。

## A. 安全：认证 / 授权 / 注入

- [x] 🔴 **代码执行工具无沙箱（RCE）** — `agent/tool/CodeExecutionTool.java` 使用 `engine.eval(code)` 直接执行脚本，可访问 Java 反射/`Runtime.exec`，等价任意命令执行；且 `timeout_ms` 声明但未强制，ScriptEngine 为单例线程不安全。需替换为真正沙箱（GraalVM `js` 且 `allowAllAccess(false)` + 资源/时间限制）或直接下线该工具。
- [x] 🔴 **`/ai/tool/execute` 缺少角色校验** — `controller/AiToolController.java` 的 execute 端点无 `@SaCheckRole`，任何登录用户可触发 `code_execution` 工具 = RCE。补充管理员角色校验并对内置危险工具做白名单。
- [x] 🔴 **计算器 JS 注入** — `agent/tool/CalculatorTool.java` 用 `engine.eval` 执行表达式，可注入任意 JS；同时 fallback 分词器（L84-104）只收集字母与点、**丢弃数字**，导致降级解析器完全不可用。改用纯数学表达式解析库（如 exp4j）。
- [x] 🔴 **自定义工具 SSRF** — `agent/ToolExecutionService.java#executeCustom`（L91-134）对用户配置 URL 无任何校验，可访问内网/元数据地址；且 GET 参数未 URL 编码。需加 URL 白名单/内网地址拦截 + 参数编码。
- [x] 🔴 **Web 爬取 / STT SSRF** — `service/rag/WebScraperService.java#scrape`、`service/rag/impl/SttServiceImpl.java`（`HttpUtil.downloadBytes(audioUrl)`）对外部 URL 无校验且无大小限制，存在 SSRF + 大响应内存耗尽。统一封装带内网拦截与大小上限的下载器。
- [x] 🟠 **Agent Chat 接口缺角色/权限校验** — `controller/AgentChatController.java` `/ai/agent/chat` 已被全局登录拦截器保护（非“无认证”），但无 `@SaCheckRole`，任何登录用户可调用任意 Agent（含 `code_execution` 工具）。建议补角色校验并对危险工具做白名单。
- [x] 🔴 **会话接口越权（IDOR）** — `controller/ConversationController.java` 的 `delete/messages/saveMessage/deleteMessage/clearMessages/share/truncateMessages/export` 全部只按传入 id 操作，**不校验会话归属当前用户**，任意用户可读取/删除他人会话。需在 Service 层统一按 `userId` 过滤。
- [x] 🔴 **API Key 加密使用公开已知密钥** — `util/ApiKeyCipher.java` 未配置 `myapp.ai.api-key-secret` 时回退源码内置常量 `DEFAULT_SECRET`；而 cyreneai-api 模块 `app.yml` 配置的值恰好**等于该默认常量**、starter 模块 `app.yml` 又未配置 → 供应商密钥实际以公开已知密钥加密，加密形同虚设；且 Hutool `SecureUtil.aes` 默认 ECB 模式不安全。生产强制要求外部配置强随机密钥并改用 GCM/CBC+IV。
- [x] 🟠 **API Key 管理接口鉴权不一致** — `controller/AiApiKeyController.java` 的 `listByPage`、`usage/{apiKeyId}` 缺少 `@SaCheckRole`，普通用户可列出全部密钥元数据与用量，且 usage 无归属校验（IDOR）。
- [x] 🟠 **RAG / 图片 / Embedding 端点缺角色校验** — 文档抓取(`/ai/webScraper/scrape`)、embeddings(`/ai/embeddings`)、图片生成(`/ai/image/generate`)等端点已受全局登录拦截，但缺 `@SaCheckRole`（同模块其他管理端点已加），建议统一补齐角色校验。

## B. Chat 核心（ChatController / useChatStream）

- [x] 🔴 **配额 / 限流完全未生效** — `governance/RateLimitService`、`AiQuotaAlertService.evaluate`、`AiApiKeyService.verifyPlainKey` 均**未被任何控制器调用**（全局搜索 0 引用）。`ChatController.stream` 未做任何配额/RPM/TPM/月度 Token 检查，治理层为死代码。需在 chat/agent 主链路接入 check + increment。
- [x] 🔴 **月度 Token 配额永不累加** — `RateLimitServiceImpl.increment` 只写 Redis 的 RPM/TPM，从不回写 `ai_api_key.tokens_used_this_month`，即使接入 check 也永远判定未超限。
- [x] 🟠 **输出内容未做安全审核** — `ChatController` 仅对输入调用 `contentSafetyService.check(...,"input")`，流式输出内容从不过滤，`ContentSafetyService` 的 `output` 分支形同虚设。
- [x] 🟠 **`latch.await()` 无超时** — `ChatController.stream`（L272）无超时等待，底层模型若不回调 onComplete/onError 将永久阻塞请求线程，存在线程耗尽风险。改为 `await(timeout)`。
- [x] 🟠 **未返回用量的供应商成本统计失效** — 仅当 `response.metadata().tokenUsage()` 非空才记录 token/成本（L210），Ollama/custom 等不回传用量时 token=0，成本与审计数据缺失。需按字符估算兑底。
- [x] 🟠 **消息持久化时序风险** — 用户消息在流式**完成后**才 `saveMessages`（L282），客户端中断或服务崩溃时用户消息丢失；停止流时服务端仍保存部分助手内容，与前端显示的 `[stopped]` 不一致。
- [x] 🟠 **截断逻辑越界风险** — `compressMessages` 在无 SystemMessage 且触发截断时 `truncated.add(1, msg)`（L624）对空列表插入索引 1，将抛 `IndexOutOfBoundsException`。
- [x] 🟡 **Token 估算过糙** — 统一按“4 字符/token”估算（L590），对中文严重低估，可能超出上下文窗口。
- [x] 🟡 **审计明文留存用户输入** — `recordAudit` 记录最多 500 字用户原文（`promptSnippet`），涉隐私，建议脱敏或可配置。
- [x] 🟡 **ChatModel 构建逻辑重复** — `buildStreamingChatModel` 与 `buildChatModel` 大量重复；`custom` 类型未校验 `baseUrl` 为空。建议抽工厂。
- [x] 🟠 **前端多模态图片丢失** — `composables/useChatStream.ts` 组装 payload 时只取 `{role, content}`（L67-70），丢弃图片 `contentParts`，导致图片对话在该链路失效（后端已支持 `image_url`）。
- [x] 🟠 **Token 请求头不一致** — 前端 `axios-util.ts` / `useChatStream.ts` 使用 `sa-token` 头，而 `app.yml` 中 `sa-token.token-name: Authorization`，agent-chat 又从未写入的 `localStorage['sa-token']` 取值 → 认证头混乱、agent 对话请求无有效 token。统一为 `Authorization`。

## C. Agent 与工具框架

- [x] 🔴 **工具依赖注入失效导致 NPE** — 工具经反射 `newInstance()` 实例化（`ToolExecutionService`），`KnowledgeBaseTool` 等的 `@Inject` 字段为 null，调用即 NPE。改为从容器获取 Bean 或手动注入依赖。
- [x] 🟠 **无重复工具调用检测** — `AgentService` ReAct 循环无重复调用/环路检测，模型可能反复调用同一工具直至 `MAX_ITERATIONS`（=10）耗尽。
- [x] 🟠 **脆弱的 JSON 提取** — `AgentService.extractJsonBlock` 用 `indexOf('{')`/`lastIndexOf('}')` 提取工具调用，含代码块/多 JSON 时易解析错误。
- [x] 🟠 **SSE 流未在 finally 关闭** — `AgentChatController` 的 OutputStream 无 finally 兜底关闭，异常路径可能泄漏连接。
- [x] 🟠 **同步阻塞占用 SSE 线程** — `AgentService` 中 `chatModel.chat(messages)` 同步阻塞在 SSE 处理线程内，并发下线程占用高。
- [x] 🟡 **WebSearchTool 有请求无解析** — `agent/tool/WebSearchTool.java` 虽发起 Google/Bing 请求但从不解析返回 HTML，仅回固定提示文本，等同占位 stub，与 README“网页搜索”不符。
- [x] 🟡 **WeatherTool 空值风险** — 直接访问外部 JSON 字段，字段缺失时 NPE。

## D. RAG / 知识库 / 文档 / 向量

- [ ] 🔴 **文档文件路径不匹配导致索引永久失败** — `service/rag/impl/DocumentServiceImpl.java` L62 用 `System.currentTimeMillis()+"_"+fileName` 落盘，L73 又调用一次 `System.currentTimeMillis()` 生成入库 `filePath`，两个时间戳不同 → DB 记录路径与磁盘文件永不一致，reIndex/processDocument 必然失败。改为复用同一变量。
- [ ] 🔴 **文档上传路径遍历** — `DocumentServiceImpl` 未净化 `fileName`，`../` 可写出上传目录；同时缺文件大小/类型校验。
- [ ] 🟠 **无界线程创建** — `DocumentServiceImpl`（L86、L199）用 `new Thread().start()` 处理索引，高并发下线程暴涨。改用受控线程池。
- [ ] 🟠 **知识库列表全表加载内存分页** — `KnowledgeBaseServiceImpl.listByPage`（L92-103）加载全表后内存分页，数据量大时 OOM/慢。改用 DB 分页。
- [ ] 🟠 **检索 N+1 查询** — `KnowledgeBaseServiceImpl.retrieve`（L158-161）对 docIds 逐条 `load` 查询文档名（`listEnabled` 无此问题），改为批量 IN 查询或联表。
- [ ] 🟠 **向量批量写入逐条 INSERT** — `service/rag/impl/PgVectorStore.java#addChunks`（L34-41）每 chunk 单独 INSERT，改批量写入。
- [ ] 🟠 **文本分块死循环风险** — `service/rag/TextSplitter.java#splitBySize`（L22）`start += chunkSize - overlap`，当 `overlap >= chunkSize` 时步进 ≤0，死循环。需校验 overlap < chunkSize。
- [ ] 🟠 **Embedding 模型缓存永不失效** — `EmbeddingServiceImpl` 的 `modelCache` 无过期，供应商密钥/配置更新后仍用旧实例。加入 TTL / 基于 updateTime 失效。
- [ ] 🟠 **图片服务错误当成功返回** — `ImageServiceImpl` 将错误信息作为普通字符串返回，控制器包成 `ApiResult.ok`，前端无法区分错误与图片 URL。应抛异常/返回错误码。
- [ ] 🟡 **TTS 格式与扩展名不符** — `TtsServiceImpl` DashScope 合成 WAV（L172）却存为 `.mp3`（L237）；且 `uploads/audio/tts/` 音频无清理策略，长期累积。
- [ ] 🟡 **STT 无响应大小限制** — `readAllBytes`（L84）无上限，超大音频可致 OOM。
- [ ] 🟡 **synthesize-url 未返回音频地址** — TTS 的 url 合成接口未回传可访问的音频 URL。

## E. 治理（审核 / 审计 / 密钥）

- [ ] 🟠 **外部 Moderation 未实现** — `ContentSafetyServiceImpl`（L86-89）对 `openai_moderation`/`dashscope` 仅打日志未实现，配置此类规则将静默放行，与“Moderation API”宣称不符。
- [ ] 🟠 **审核 replace 动作破坏整条文本** — `ContentSafetyServiceImpl`（L94）命中规则 replace 时 `working = "***"`，把整条消息替换为 `***` 而非仅替换命中片段。
- [ ] 🟠 **敏感词检查大小写敏感 + 无缓存 + 线性扫描** — `contains(word)` 区分大小写可被绕过；每次请求 `listEnabled()` 全量查库 + 逐词 `contains`，性能差。改为忽略大小写、缓存词表、Aho-Corasick。
- [ ] 🟠 **限流计数非原子** — `RateLimitServiceImpl` 用 get→save 两步更新计数（非原子），高并发超发；应改用 Redis `INCR/INCRBY + EXPIRE`。
- [ ] 🟡 **配额告警仅打日志** — `AiQuotaAlertServiceImpl.evaluate` 命中后 TODO 未接入 email/webhook，告警无实际触达（且该方法本身未被调用）。

## F. 前端（Vue3 / TS）

- [ ] 🔴 **Markdown 渲染 XSS** — `views/ai/chat/index.vue`、`views/ai/agent-chat/index.vue` 对 `marked` 输出直接 `v-html` 未经 DOMPurify 消毒，模型/知识库返回的恶意内容可致存储型 XSS。渲染前统一 sanitize。
- [ ] 🔴 **路由守卫无登录校验** — `router/index.ts` `beforeEach` 仅处理 `/` 重定向，未校验登录态，未登录可直接进入所有 admin 路由。
- [ ] 🟠 **axios 顶层调用 useUserStore** — `utils/axios-util.ts` L6 在模块顶层 `useUserStore()`，若在 Pinia 激活前被导入将抛异常。应在函数内调用。
- [ ] 🟠 **maxContentLength=2000** — `axios-util.ts` L53 限制响应 2000 字节（Node 适配器语义），配置具误导性，浏览器端虽多被忽略，建议移除或设合理值。
- [ ] 🟠 **transformResponse 无 try-catch** — `axios-util.ts` L32 `JSON.parse` 无保护，后端返回非 JSON（HTML 错误页/空体）时抛未捕获异常；`validateStatus` 仅允许 200，其余状态无结构化处理。
- [ ] 🟠 **agent-chat 无 AbortController** — `views/ai/agent-chat/index.vue` fetch 无中断控制，组件卸载后仍写入已销毁组件状态，存在泄漏；且未检查 `response.ok`。
- [ ] 🟠 **reqCommonFeedback 无错误回调** — `api/ApiFeedback.ts` 请求失败时 loading 无法复位，页面可能卡在加载态。
- [ ] 🟠 **marked 已废弃 highlight 选项** — `chat/index.vue` L206-215 使用 marked v12+ 已移除的同步 `highlight` 回调，代码高亮实际失效。改用 `marked-highlight`。
- [ ] 🟡 **批量删除未校验空选择** — `model/tool/agent` 三处 `onDeleteBatch` 未判空选中项，空数组也会发起删除请求。
- [ ] 🟡 **流式每 chunk 全量拷贝数组** — `useChatStream.ts` / `chat/index.vue` 每个 chunk `messages.value = [...messages.value]`，长对话性能差。
- [ ] 🟡 **`response.body!` 非空断言** — `useChatStream.ts` L111 body 可能为 null，运行时崩溃风险。
- [ ] 🟡 **localStorage 明文 token** — `stores/user.ts` token 明文存 localStorage，结合 XSS 可被窃取。
- [ ] 🟡 **App.vue 恢复缓存无 try-catch** — `App.vue` L11 `JSON.parse(localStorage)` 无保护，数据损坏致白屏。
- [ ] 🟡 **中文输入法误发送** — `chat/index.vue` L176 `@keydown.enter.exact` 未处理 IME 组合，中文候选确认会误触发发送。
- [ ] 🟡 **列表以 index 为 key** — `chat/index.vue` `v-for :key="idx"`，删除消息后 key 变化引发不必要 DOM 重建。
- [ ] 🟡 **pageSize=999 模拟全量** — `agent/index.vue` 加载模型/工具用 `pageSize:999`，数据量大时丢数据/性能差。
- [ ] 🟡 **menu store 类型不安全** — `stores/menu.ts` `tabItems/menus` 无泛型，推断为 `never[]`。
- [ ] 🟡 **treeMap 用 map 执行副作用** — `utils/list-util.ts` 用 `Array.map` 做遍历副作用，应用 `forEach`。

## G. 通用工程优化

- [ ] 🟡 **敏感配置硬编码** — `app.yml` 明文数据库密码、缺少 `myapp.ai.api-key-secret`，建议改为环境变量/外部密钥管理。
- [ ] 🟡 **文件上传统一治理** — 上传（文档/音频/图片）缺统一的大小、类型、文件名净化与存储配额策略。
- [ ] 🟡 **外部调用统一封装** — SSRF 防护、超时、大小限制、重试应抽公共 HTTP 客户端，避免各处散落 `HutoolHttp`/Jsoup 直连。

