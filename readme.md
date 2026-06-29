# CyreneAI

Solon + Vue3 构建的 AI 管理后台。支持多模型供应商（OpenAI、Anthropic、Ollama、Gemini、DashScope）、
SSE 流式对话、RAG 知识库检索、Agent 编排与工具调用，提供模型/会话/知识库/工具/Agent 的统一管理界面。

> 90% 业务代码由 AI 生成。

## 功能特性

### 🤖 多模型 Chat
- 多供应商支持：OpenAI、Anthropic、Ollama、Google Gemini、DashScope、自定义 OpenAI 兼容 API
- SSE 流式输出、Markdown 渲染、代码高亮
- 对话参数控制（temperature、topP、maxTokens、system prompt）
- Token 用量统计与花费计算
- 消息编辑/删除、对话历史管理
- 左侧对话列表面板

### 📚 RAG 知识库
- 向量数据库（pgvector）接入
- Embedding 模型管理（DashScope / OpenAI）
- 文档上传与解析（PDF、DOCX、TXT、Markdown）
- 知识库 QA 检索
- Web 爬取（Jsoup）

### 🧩 Agent / 工具调用
- ReAct 模式 Agent 循环（SSE 流式）
- 8 个内置工具：计算器、日期时间、网页搜索、知识库检索、代码执行、图片生成、图片识别、天气查询
- 自定义工具注册（HTTP API，支持 Bearer / Basic 认证）
- 工具测试沙盒
- Agent 管理 CRUD + 工具关联
- Agent 运行日志与 Token 用量统计

### 🎨 多模态（规划中）
- 图片生成（DALL-E 3 / Stable Diffusion）
- 图片理解（GPT-4V / Qwen-VL / Claude 3）
- 语音合成 / 语音识别

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| **后端框架** | Solon | 3.7.2 |
| **ORM** | Sqltoy (LightDao) | 5.6.56 |
| **权限** | Sa-Token | 1.44.0 |
| **AI SDK** | LangChain4j | 1.16.3 |
| **数据库** | MySQL / PostgreSQL | 8.0+ |
| **缓存** | Redis | 6+ |
| **前端框架** | Vue 3 + TypeScript | - |
| **UI 库** | Element Plus | - |
| **构建工具** | Maven | 3.9+ |
| **Node 版本** | Node.js | 18+ |
| **JDK** | Java 21 | 21+ |

## 项目结构

```
CyreneAI/
├── cyrene-modules/               # ⭐ 业务模块（主要开发区域）
│   ├── cyreneai-api/             # AI 接口层（Controller、DTO、VO）
│   │   └── src/main/java/.../
│   │       ├── controller/       #   控制器（Chat、Model、Agent、Tool、KnowledgeBase...）
│   │       └── App.java          #   AI 模块启动入口（端口 9200）
│   ├── cyreneai-biz/             # AI 业务层（Service、PO、agent 框架）
│   │   └── src/main/java/.../
│   │       ├── agent/            #   Agent 框架核心
│   │       │   ├── AgentService.java         # ReAct 循环 + SSE 流式
│   │       │   ├── ToolSpecification.java    # 工具定义 Schema
│   │       │   ├── ToolExecutor.java         # 工具执行接口
│   │       │   ├── ToolExecutionService.java # 工具注册与路由
│   │       │   └── tool/                     # 8 个内置工具实现
│   │       ├── model/po/         #   数据库实体
│   │       ├── model/dto/        #   请求参数
│   │       ├── model/vo/         #   响应视图
│   │       ├── service/          #   服务实现
│   │       │   ├── impl/
│   │       │   └── rag/          #   知识库（Embedding、检索、文档解析、Web爬取）
│   │       └── resources/sqltoy/ #   SQLXML 查询文件
│   └── cyreneai-core/            # 预留核心能力扩展
│
├── cyrene-common/                # 公共模块（工具类、ApiResult、异常定义）
├── cyrene-service-system/        # 系统服务（用户、角色、菜单、日志）
├── cyrene-starter-solon/         # Solon 启动器（app.yml、全局过滤器、端口 9000）
├── cyrene-ui/                    # 前端 Vue3 项目
│   └── src/
│       ├── api/                  # API 接口层
│       ├── views/ai/             # AI 功能页面
│       │   ├── chat/             #   对话聊天
│       │   ├── model/            #   模型管理
│       │   ├── model-provider/   #   供应商管理
│       │   ├── tool/             #   工具管理 + 测试沙盒
│       │   ├── agent/            #   Agent 管理
│       │   ├── agent-chat/       #   Agent 对话
│       │   ├── knowledge-base/   #   知识库管理
│       │   ├── document/         #   文档管理
│       │   └── embedding-model/  #   Embedding 模型管理
│       ├── composables/          # 可组合逻辑（useChatStream）
│       ├── stores/               # Pinia 状态管理
│       ├── router/               # 路由配置
│       └── components/           # 通用组件
│
├── doc-api/                      # API 文档
├── doc-img/                      # 文档图片
├── scripts/                      # 数据库脚本
│   ├── ddl.sql                   #   完整建表 DDL
│   └── init-data.sql             #   初始化数据（菜单、角色、内置工具）
├── todo.md                       # 功能路线图
├── CONTRIBUTING.md               # 开发规范
└── pom.xml                       # Maven 父工程
```

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.9+
- Node.js 18+
- MySQL 8.0+
- Redis 6+

### 初始化数据库

```bash
mysql -u root -p < scripts/ddl.sql
mysql -u root -p < scripts/init-data.sql
```

### 启动后端

**方式一：完整启动（含 Admin 底座，端口 9000）**

```bash
mvn compile -pl cyrene-starter-solon -am -DskipTests
```
或通过 IDE 运行 `net.cocotea.cyreneadmin.SolonStarter`

**方式二：仅 AI 模块（端口 9200，可独立部署）**

```bash
mvn compile -pl cyrene-modules/cyreneai-api -am -DskipTests
```
或通过 IDE 运行 `net.cocotea.cyreneai.App`

### 启动前端

```bash
cd cyrene-ui
pnpm install
pnpm run dev
```

前端默认运行在 `http://localhost:8200`，开发时通过 `vite.config.ts` 中的 proxy 将 `/api` 代理到后端端口。

## 配置说明

### 数据库连接

编辑 `cyrene-starter-solon/src/main/resources/app.yml`：

```yaml
solon.dataSources:
  cyrene!:
    driverClassName: com.mysql.cj.jdbc.Driver
    jdbcUrl: jdbc:mysql://localhost:3306/cyrene_ai?useSSL=false&characterEncoding=utf-8
    username: root
    password: your_password
```

### AI 模型供应商配置

通过管理界面添加模型供应商和模型，支持：

- **OpenAI** — 配置 API Key 和 Base URL
- **Anthropic** — 配置 API Key
- **Ollama** — 配置本地 Base URL（默认 `http://localhost:11434`）
- **Google Gemini** — 配置 API Key
- **DashScope（通义千问）** — 配置 API Key
- **自定义兼容 API** — 配置 Base URL + API Key

### 内置工具环境变量

| 工具 | 环境变量 | 说明 |
|------|----------|------|
| 网页搜索 | `GOOGLE_API_KEY` / `BING_API_KEY` | Search API 密钥 |
| 图片生成 | `OPENAI_API_KEY` | DALL-E 3 API 密钥 |
| 图片识别 | `OPENAI_API_KEY` | GPT-4V API 密钥 |

## API 概览

### Chat
| 端点 | 方法 | 说明 |
|------|------|------|
| `/ai/chat/stream` | POST | SSE 流式对话 |
| `/ai/chat/ping` | GET | 健康检查 |
| `/ai/conversation/*` | GET/POST | 会话管理 |

### 模型管理
| 端点 | 方法 | 说明 |
|------|------|------|
| `/ai/model/listEnabled` | GET | 已启用模型列表 |
| `/ai/model/add` | POST | 新增模型 |
| `/ai/model/update` | POST | 更新模型 |
| `/ai/model/deleteBatch` | POST | 批量删除 |
| `/ai/model/listByPage` | POST | 分页查询 |
| `/ai/model-provider/*` | GET/POST | 供应商管理 |

### 工具管理
| 端点 | 方法 | 说明 |
|------|------|------|
| `/ai/tool/listByPage` | POST | 分页查询 |
| `/ai/tool/add` | POST | 新增工具 |
| `/ai/tool/update` | POST | 更新工具 |
| `/ai/tool/deleteBatch` | POST | 批量删除 |
| `/ai/tool/execute` | POST | 执行测试 |
| `/ai/tool/specifications` | GET | 获取工具规范列表 |
| `/ai/tool/listEnabled` | GET | 已启用的工具列表 |

### Agent 管理
| 端点 | 方法 | 说明 |
|------|------|------|
| `/ai/agent/listByPage` | POST | 分页查询 |
| `/ai/agent/add` | POST | 新增 Agent |
| `/ai/agent/update` | POST | 更新 Agent |
| `/ai/agent/deleteBatch` | POST | 批量删除 |
| `/ai/agent/chat` | POST | SSE Agent 对话 |
| `/ai/agent/listEnabled` | GET | 已启用的 Agent 列表 |

### 知识库
| 端点 | 方法 | 说明 |
|------|------|------|
| `/ai/knowledge-base/*` | GET/POST | 知识库 CRUD |
| `/ai/knowledge-base/retrieve` | POST | 知识检索 |
| `/ai/document/*` | GET/POST | 文档管理 |
| `/ai/embedding-model/*` | GET/POST | Embedding 模型管理 |

## 功能路线图

详见 [todo.md](./todo.md)：

- ✅ Phase 0: Chat 基础
- ✅ Phase 1: Chat 功能完善
- ✅ Phase 2: 会话管理
- ✅ Phase 3: RAG / 知识库
- ✅ Phase 4: Agent / 工具调用
- 🔄 Phase 5-9: 多模态、Prompt 管理、监控等（规划中）

## License

MIT

## 相关项目

[CyreneAdmin](https://gitee.com/momoljw/CyreneAdmin) — Admin 管理底座
