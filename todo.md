# CyreneAI — AI Feature Roadmap

## Overview
该项目是一个基于 Solon + Vue3 的 AI 平台，当前已完成基础 Chat 功能骨架，以下列出需要补充的 AI 基础功能，按阶段划分。

---

## Phase 0 — 已完成（Chat 基础）
- [x] `POST /ai/chat/stream` SSE 流式聊天接口 ✔
- [x] `GET /ai/model/listEnabled` 已启用模型列表 ✔
- [x] 模型供应商 CRUD（前端 + 后端） ✔
- [x] 模型 CRUD（前端 + 后端） ✔
- [x] Chat UI（Markdown 渲染、代码高亮、流式展示） ✔
- [x] `useChatStream` 流式 Composable ✔
- [x] 基础路由与菜单 ✔

---

## 🔧 Phase 1 — Chat 功能完善（修复当前问题）

### 1.1 Sa-Token 认证放行
- [x] `app.yml` 中为 `/ai/chat/stream`、`/ai/chat/ping`、`/ai/model/listEnabled` 添加 Sa-Token exclude ✔
- [x] `useChatStream.ts` 流式请求携带 auth token（当前使用裸 `fetch()` 未带 token） ✔

### 1.2 多模型供应商支持（后端）
- [x] **OpenAI** — `OpenAiStreamingChatModel` 适配 ✔
- [x] **Anthropic** — `AnthropicStreamingChatModel` 适配 ✔
- [x] **Ollama** — `OllamaStreamingChatModel` 适配（本地模型） ✔
- [x] **Google Gemini** — `GeminiStreamingChatModel` 适配 ✔
- [x] **自定义 OpenAI 兼容 API** — 通过 `baseUrl` + `apiKey` 动态构建 ✔
- [x] **工厂模式** 重构 `buildStreamingChatModel()`，根据 `providerType` 路由到对应的 ChatModel 构建器 ✔

### 1.3 对话参数控制
- [x] `ChatRequestDTO` 增加 `temperature`、`topP`、`maxTokens`、`systemPrompt` 字段 ✔
- [x] 前端聊天界面增加参数调节面板（折叠式） ✔
- [x] 后端将参数透传给底层 ChatModel ✔

### 1.4 Token 用量统计
- [x] 每次 Chat 完成时记录 `promptTokens`、`completionTokens`、`totalTokens` ✔
- [x] 关联模型定价计算本次花费 ✔
- [x] 前端展示 Token 用量（每次回复尾部小字） ✔

### 1.5 消息编辑 / 删除
- [x] 用户可编辑已发送的消息（重新生成回复） ✔
- [x] 用户可删除单条消息或清空当前对话 ✔
- [x] 删除后重新生成保持对话上下文一致性 ✔

### 1.6 对话历史 - 左侧面板
- [x] 左侧对话列表面板（已提及但未实现） ✔
- [x] 新建对话、切换对话、删除对话 ✔
- [x] 对话标题自动生成（基于首条消息） ✔

---

## 🗄 Phase 2 — 对话 & 会话管理

### 2.1 持久化会话
- [x] `ai_conversation` 表：id, title, user_id, model_id, system_prompt, created_time, updated_time ✔
- [x] `ai_message` 表：id, conversation_id, role, content, tokens, created_time ✔
- [x] 后端 Conversation CRUD Service + Controller ✔
- [x] 前端对话历史从后端加载而非纯内存 ✔

### 2.2 上下文窗口管理
- [x] 根据模型的 `contextWindow` 进行 token 计数 ✔
- [x] 超出窗口上限时自动截断（丢弃最早的消息） ✔
- [x] 可选择摘要压缩策略（用 LLM 总结历史后再拼接） ✔

### 2.3 会话导出 / 导入
- [x] 导出为 Markdown / JSON / TXT（当前仅支持 JSON） ✔
- [x] 从 JSON 导入恢复历史对话 ✔
- [x] 分享对话（生成只读链接） ✔

---

## 📚 Phase 3 — RAG / 知识库

### 3.1 向量数据库接入
- [x] 引入向量数据库依赖（pgvector / Milvus / Chroma） ✔
- [x] 配置向量数据库连接 ✔
- [x] 向量存取基础 Service 封装（`VectorStore`） ✔

### 3.2 Embedding 模型接入
- [x] 支持 DashScope 文本嵌入（`TextEmbeddingModel`） ✔
- [x] 支持 OpenAI Embedding ✔
- [x] `ai_embedding_model` 表管理嵌入模型配置 ✔
- [x] Embedding API 端点 `POST /ai/embeddings` ✔

### 3.3 文档管理
- [x] `ai_document` 表：id, name, type(pdf/docx/txt/md), size, status, chunk_count ✔
- [x] 文件上传接口（支持 PDF / DOCX / TXT / MD） ✔
- [x] 文档解析服务（文本提取） ✔
- [x] 文档分块策略配置（按大小 / 按段落 / 递归分割） ✔
- [x] 分块入库（生成 Embedding 并存入向量库） ✔
- [x] 前端文档管理页面（上传、列表、删除、重新索引） ✔

### 3.4 知识库 QA
- [x] `ai_knowledge_base` 表：id, name, description, model_id, chunk_size, overlap ✔
- [x] 知识库与文档关联（多对多） ✔
- [x] 检索策略（相似度 top-k、MMR、混合检索） ✔
- [x] Rerank 重排序接入 ✔
- [x] 引用来源展示（前端显示匹配的文档片段） ✔
- [x] Chat 时自动检索知识库并注入上下文 ✔

### 3.5 Web 爬取
- [x] URL 内容抓取（JSOUP / web 爬虫） ✔
- [x] 网页内容清洗（去除导航、广告） ✔
- [x] 网页转文档入库 ✔

---

## 🤖 Phase 4 — Agent / 工具调用

### 4.1 Function Calling 基础
- [x] 工具定义 Schema 接口（`ToolSpecification`） ✔
- [x] 文本函数调用协议（JSON-based，兼容所有供应商） ✔
- [x] 支持 OpenAI / Anthropic / Ollama / Gemini / DashScope / 自定义 ✔
- [x] `ToolExecutor` 接口 + 反射自动注册 ✔
- [x] `ToolSpecification` 定义工具 Schema ✔

### 4.2 内置工具
- [x] **计算器**（数学表达式求值，内置解析器，兼容 GraalVM JS） ✔
- [x] **当前时间/日期**（时区感知） ✔
- [x] **网页搜索**（Google / Bing Search API） ✔
- [x] **知识库检索**（调用 RAG 能力） ✔
- [x] **代码执行**（JavaScript 沙箱，支持 GraalVM JS） ✔
- [x] **图片生成**（DALL-E 3） ✔
- [x] **图片识别**（GPT-4V / GPT-4o） ✔
- [x] **天气查询**（wttr.in） ✔

### 4.3 自定义工具
- [x] `ai_tool` 表：name, description, schema(json), url, auth_type, http_method ✔
- [x] 用户可注册自定义 API 作为工具（支持 Bearer / Basic 认证） ✔
- [x] 工具管理 CRUD 页面（含测试沙箱） ✔

### 4.4 Agent 编排
- [x] ReAct 模式 Agent 循环（SSE 流式） ✔
- [x] `ai_agent` 表：name, description, model_id, tools[], system_prompt, max_iterations ✔
- [x] Agent 运行日志与 Token 用量统计（`ai_agent_log` 表） ✔
- [x] Agent 管理 CRUD 页面（关联工具） ✔
- [x] Agent Chat UI（SSE 流式：thinking → tool_call → tool_result → content） ✔

---

## 🎨 Phase 5 — 多模态

### 5.1 图片生成
- [x] 支持 DALL-E 3（OpenAI Image Model）
- [x] 支持 Stable Diffusion（通过 API）
- [x] 支持多模态模型生成图片（如 GPT-4o、Gemini 等，可配置）
- [x] 图片生成模型配置管理（provider、model、apiKey、参数）
- [x] 前端图片生成页面（prompt 输入、风格选择、尺寸选择、模型选择）
- [x] 生成历史记录
- [x] 生成的图片可引用到 Chat 中

### 5.2 图片理解（Vision）
- [x] Chat 中支持上传图片
- [x] 多模态模型接入（GPT-4V、Qwen-VL、Claude 3、Gemini Pro Vision 等，可配置）
- [x] 图片识别模型配置管理（provider、model、apiKey、参数）
- [x] 图片 Base64 / URL 转 Message Content

### 5.3 语音合成（TTS）
- [x] OpenAI TTS / DashScope 语音合成接入
- [x] 前端语音播放
- [x] 对话内容转语音下载

### 5.4 语音识别（STT）
- [x] Whisper API 接入
- [x] 前端语音录制上传
- [x] Chat 语音输入

---

## 🧩 Phase 6 — Prompt 管理

### 6.1 提示词模板
- [x] `ai_prompt_template` 表：name, content, variables[], category
- [x] 模板变量替换引擎（`{{variable}}` 语法）
- [x] 提示词模板 CRUD 页面

### 6.2 系统提示词
- [x] 对话级别 system prompt 编辑
- [x] 预设 system prompt 快速选择
- [x] 模型默认 system prompt 配置

### 6.3 Prompt 版本管理
- [x] 模板版本历史
- [x] A/B 测试支持
- [x] Prompt 效果评估

---

## 🔐 Phase 7 — 管理与治理

### 7.1 API Key 管理
- [x] 用户级 API Key 生成（用于外部调用 AI 接口）
- [x] Key 权限范围限制（可用模型、速率限制）
- [x] Key 调用统计面板

### 7.2 使用配额
- [x] 用户/Key 级别速率限制（RPM / TPM）
- [x] 月度 Token 配额
- [x] 配额超限告警

### 7.3 审计日志
- [x] 所有 AI 请求记录到 `ai_audit_log` 表
- [x] 记录：用户、模型、tokens、耗时、状态
- [x] 审计日志查询页面

### 7.4 内容安全
- [x] 敏感词过滤
- [x] 输入/输出内容审核（Moderation API）
- [x] 拒绝策略配置（拦截/替换/警告）

---

## 📊 Phase 8 — 监控与观测

### 8.1 模型调用面板
- [x] Token 使用趋势图（日/周/月）
- [x] 模型调用次数排行
- [x] 用户调用排行
- [x] 平均响应延迟监控

### 8.2 成本分析
- [x] 按模型/用户/时间维度的花费统计
- [x] 预算设置与超支告警
- [x] 成本优化建议

### 8.3 模型评估
- [x] 在线模型测试/Playground
- [x] 模型输出对比（并排对比不同模型回复）
- [x] 评估数据集管理

---

## 🏗 Phase 9 — 工程优化

### 9.1 API Key 加密存储
- [x] `ai_model_provider.apiKey` AES 加密存储 ✔
- [x] 前端展示时脱敏（`sk-****...ab12`） ✔
- [x] 密钥管理（密钥轮换） ✔

### 9.2 性能优化
- [x] 流式响应背压控制 ✔
- [x] 模型实例缓存（减少重复构建 ChatModel） ✔
- [x] 大文本分段处理 ✔

---

## 技术栈决策备注
| 模块 | 建议方案 |
|------|----------|
| 向量数据库 | pgvector（与现有 MySQL/PostgreSQL 配合） |
| Embedding | DashScope Text-Embedding / OpenAI Embedding |
| Rerank | Cohere Rerank / BGE Rerank |
| 文档解析 | Apache Tika / PyMuPDF（PDF）/ Apache POI（DOCX） |
| TTS / STT | OpenAI Whisper + TTS / DashScope 语音 |
| Web 爬取 | Jsoup / Crawler4j |
| 图片生成 | OpenAI DALL-E 3 / Stability AI |
| Agent | langchain4j ToolSpecification + ToolExecution |
