# AI 编码指南

## 核心流程

每次编码需求遵循以下步骤：

```
读懂需求 → 理解上下文 → 制定方案 → 实现代码 → 验证正确性 → 生成 Commit Msg
```

## 需求分析

1. **先读 todo.md** — 了解当前阶段和未完成任务，将任务拆解为可执行的子步骤
2. **理解现有代码** — 通过 `task` 工具探索相关模块，不重复造轮子
3. **确认技术方案** — 涉及数据库改动的必须先确认 `scripts/ddl.sql` 和现有实体

## 编码规范

- 遵守 `CONTRIBUTING.md` 中的模块职责和 API 规范
- **少于 3 个参数用 `@Param` + 路径变量，不用 `@Body`**
- 后端新增字段必须同步更新 `scripts/ddl.sql`
- 前端 API 调用统一使用 `src/utils/axios-util.ts` 的 `request()`

## 验证

- 完成后执行 `mvn compile` 检查后端编译（如环境允许）
- 确认代码逻辑完整性，无遗漏的 TODO 或空 catch

## 生成 Commit Msg

**每完成一个独立需求，必须生成一条符合 Conventional Commits 格式的提交信息。**

格式：
```
<type>: <简短描述>

<可选详细说明>
```

| 类型 | 适用场景 |
|------|----------|
| `feat` | 新功能 |
| `fix` | 修复 bug |
| `refactor` | 重构 |
| `perf` | 性能优化 |
| `docs` | 文档 |
| `style` | 代码格式 |
| `chore` | 构建/工具 |

示例：
```
feat: support context window summarization strategy
fix: use @Param instead of @Body for single-param endpoints
docs: add CONTRIBUTING.md and AI coding guide
```

## 前后端对应关系

| 后端 | 前端 |
|------|------|
| `cyrene-modules/cyreneai-api/src/main/java/.../controller/` | `src/api/` |
| `cyrene-modules/cyreneai-biz/src/main/java/.../model/po/` | 数据库表（无前端对应） |
| `src/views/` | 页面组件 |
| `src/composables/` | 复用逻辑（如 `useChatStream`） |

---

**每次编码完成后，在终端的对话输出提示中提供 commit msg，但不执行 `git commit`。**
