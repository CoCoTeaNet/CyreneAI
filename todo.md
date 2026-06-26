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
- [ ] 关联模型定价计算本次花费
- [x] 前端展示 Token 用量（每次回复尾部小字） ✔

### 1.5 消息编辑 / 删除
- [ ] 用户可编辑已发送的消息（重新生成回复）
- [ ] 用户可删除单条消息或清空当前对话
- [ ] 删除后重新生成保持对话上下文一致性

### 1.6 对话历史 - 左侧面板
- [x] 左侧对话列表面板（已提及但未实现） ✔
- [x] 新建对话、切换对话、删除对话 ✔
- [ ] 对话标题自动生成（基于首条消息）

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
- [ ] 可选择摘要压缩策略（用 LLM 总结历史后再拼接）

### 2.3 会话导出 / 导入
- [ ] 导出为 Markdown / JSON / TXT（当前仅支持 JSON）
- [x] 从 JSON 导入恢复历史对话 ✔
- [ ] 分享对话（生成只读链接）

---

## 📚 Phase 3 — RAG / 知识库

### 3.1 向量数据库接入
- [ ] 引入向量数据库依赖（pgvector / Milvus / Chroma）
- [ ] 配置向量数据库连接
- [ ] 向量存取基础 Service 封装（`VectorStore`）

### 3.2 Embedding 模型接入
- [ ] 支持 DashScope 文本嵌入（`TextEmbeddingModel`）
- [ ] 支持 OpenAI Embedding
- [ ] `ai_embedding_model` 表管理嵌入模型配置
- [ ] Embedding API 端点 `POST /ai/embeddings`

### 3.3 文档管理
- [ ] `ai_document` 表：id, name, type(pdf/docx/txt/md), size, status, chunk_count
- [ ] 文件上传接口（支持 PDF / DOCX / TXT / MD）
- [ ] 文档解析服务（文本提取）
- [ ] 文档分块策略配置（按大小 / 按段落 / 递归分割）
- [ ] 分块入库（生成 Embedding 并存入向量库）
- [ ] 前端文档管理页面（上传、列表、删除、重新索引）

### 3.4 知识库 QA
- [ ] `ai_knowledge_base` 表：id, name, description, model_id, chunk_size, overlap
- [ ] 知识库与文档关联（多对多）
- [ ] 检索策略（相似度 top-k、MMR、混合检索）
- [ ] Rerank 重排序接入
- [ ] 引用来源展示（前端显示匹配的文档片段）
- [ ] Chat 时自动检索知识库并注入上下文

### 3.5 Web 爬取
- [ ] URL 内容抓取（JSOUP / web 爬虫）
- [ ] 网页内容清洗（去除导航、广告）
- [ ] 网页转文档入库

---

## 🤖 Phase 4 — Agent / 工具调用

### 4.1 Function Calling 基础
- [ ] 工具定义 Schema 接口（`ToolSpecification`）
- [ ] 支持 OpenAI 风格 Function Calling
- [ ] 支持 DashScope 工具调用

### 4.2 内置工具
- [ ] **计算器**（执行数学表达式）
- [ ] **当前时间/日期**
- [ ] **网页搜索**（通过 Search API）
- [ ] **知识库检索**（调用 RAG 能力）
- [ ] **代码执行**（沙箱 Python/JS 运行）
- [ ] **图片生成**（调用 Phase 5 图文生成，支持配置多模态模型如 DALL-E 3、Stable Diffusion、GPT-4o 等）
- [ ] **图片识别**（调用多模态模型如 GPT-4V、Qwen-VL、Claude 3 等，可配置）
- [ ] **天气查询**

### 4.3 自定义工具
- [ ] `ai_tool` 表：name, description, schema(json), url, auth_type
- [ ] 用户可注册自定义 API 作为工具
- [ ] 工具测试沙盒

### 4.4 Agent 编排
- [ ] ReAct 模式 Agent 循环
- [ ] `ai_agent` 表：name, description, model_id, tools[], system_prompt
- [ ] 多 Agent 协作（Agent Orchestrator）
- [ ] Agent 运行日志与监控

---

## 🎨 Phase 5 — 多模态

### 5.1 图片生成
- [ ] 支持 DALL-E 3（OpenAI Image Model）
- [ ] 支持 Stable Diffusion（通过 API）
- [ ] 支持多模态模型生成图片（如 GPT-4o、Gemini 等，可配置）
- [ ] 图片生成模型配置管理（provider、model、apiKey、参数）
- [ ] 前端图片生成页面（prompt 输入、风格选择、尺寸选择、模型选择）
- [ ] 生成历史记录
- [ ] 生成的图片可引用到 Chat 中

### 5.2 图片理解（Vision）
- [ ] Chat 中支持上传图片
- [ ] 多模态模型接入（GPT-4V、Qwen-VL、Claude 3、Gemini Pro Vision 等，可配置）
- [ ] 图片识别模型配置管理（provider、model、apiKey、参数）
- [ ] 图片 Base64 / URL 转 Message Content

### 5.3 语音合成（TTS）
- [ ] OpenAI TTS / DashScope 语音合成接入
- [ ] 前端语音播放
- [ ] 对话内容转语音下载

### 5.4 语音识别（STT）
- [ ] Whisper API 接入
- [ ] 前端语音录制上传
- [ ] Chat 语音输入

---

## 🧩 Phase 6 — Prompt 管理

### 6.1 提示词模板
- [ ] `ai_prompt_template` 表：name, content, variables[], category
- [ ] 模板变量替换引擎（`{{variable}}` 语法）
- [ ] 提示词模板 CRUD 页面

### 6.2 系统提示词
- [ ] 对话级别 system prompt 编辑
- [ ] 预设 system prompt 快速选择
- [ ] 模型默认 system prompt 配置

### 6.3 Prompt 版本管理
- [ ] 模板版本历史
- [ ] A/B 测试支持
- [ ] Prompt 效果评估

---

## 🔐 Phase 7 — 管理与治理

### 7.1 API Key 管理
- [ ] 用户级 API Key 生成（用于外部调用 AI 接口）
- [ ] Key 权限范围限制（可用模型、速率限制）
- [ ] Key 调用统计面板

### 7.2 使用配额
- [ ] 用户/Key 级别速率限制（RPM / TPM）
- [ ] 月度 Token 配额
- [ ] 配额超限告警

### 7.3 审计日志
- [ ] 所有 AI 请求记录到 `ai_audit_log` 表
- [ ] 记录：用户、模型、tokens、耗时、状态
- [ ] 审计日志查询页面

### 7.4 内容安全
- [ ] 敏感词过滤
- [ ] 输入/输出内容审核（Moderation API）
- [ ] 拒绝策略配置（拦截/替换/警告）

---

## 📊 Phase 8 — 监控与观测

### 8.1 模型调用面板
- [ ] Token 使用趋势图（日/周/月）
- [ ] 模型调用次数排行
- [ ] 用户调用排行
- [ ] 平均响应延迟监控

### 8.2 成本分析
- [ ] 按模型/用户/时间维度的花费统计
- [ ] 预算设置与超支告警
- [ ] 成本优化建议

### 8.3 模型评估
- [ ] 在线模型测试/Playground
- [ ] 模型输出对比（并排对比不同模型回复）
- [ ] 评估数据集管理

---

## 🏗 Phase 9 — 工程优化

### 9.1 API Key 加密存储
- [ ] `ai_model_provider.apiKey` AES 加密存储
- [ ] 前端展示时脱敏（`sk-****...ab12`）
- [ ] 密钥管理（密钥轮换）

### 9.2 统一异常处理
- [ ] AI 模块全局异常拦截器
- [ ] 标准错误响应格式 `{code, message, data}`
- [ ] 友好错误提示前端展示

### 9.3 性能优化
- [ ] 流式响应背压控制
- [ ] 模型实例缓存（减少重复构建 ChatModel）
- [ ] 大文本分段处理

### 9.4 测试覆盖
- [ ] 单元测试（Service 层）
- [ ] 集成测试（Controller 层）
- [ ] 前端组件测试

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
