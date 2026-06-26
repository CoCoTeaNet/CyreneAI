# 开发规范

## 模块职责

**⚠️ 核心原则：业务代码只在 `cyrene-modules` 中开发，其它模块尽量不改动。**

| 模块 | 说明 | 是否修改 |
|------|------|----------|
| `cyrene-modules/cyreneai-api` | AI Controller、主入口 App.java | ✅ 主要开发区域 |
| `cyrene-modules/cyreneai-biz` | AI Service、PO、DTO、VO | ✅ 主要开发区域 |
| `cyrene-modules/cyreneai-core` | 预留核心能力扩展 | ✅ 可扩展 |
| `cyrene-common` | 公共工具类、ApiResult、异常定义 | ⚠️ 谨慎修改 |
| `cyrene-service-system` | 系统管理（用户/角色/菜单/日志） | ❌ 不改（Admin 底座） |
| `cyrene-starter-solon` | Solon 启动配置、app.yml、全局过滤器 | ❌ 不改 |
| `cyrene-ui` | Vue3 前端 | ⚠️ 谨慎修改 |

---

## 开发环境

### 依赖
- **JDK 21** — 必须使用 Java 21
- **Maven 3.9+** — 项目构建
- **Node.js 18+** — 前端构建
- **MySQL 8.0+** — 主数据库
- **Redis 6+** — Sa-Token 会话缓存

### 启动步骤

```bash
# 1. 初始化数据库
mysql -u root -p < scripts/ddl.sql
mysql -u root -p < scripts/init-data.sql

# 2. 启动后端（主应用端口 9000）
mvn compile -pl cyrene-starter-solon -am -DskipTests
# 或通过 IDE 运行 SolonStarter.java

# 3. 启动 AI 模块（端口 9200，可选独立部署）
mvn compile -pl cyrene-modules/cyreneai-api -am -DskipTests
# 或通过 IDE 运行 App.java

# 4. 启动前端（端口 8200）
cd cyrene-ui
pnpm install
pnpm run dev
```

> 开发时可通过 `cyrene-ui/vite.config.ts` 中的 proxy 配置将 `/api` 代理到后端端口。

---

## 代码规范

### 后端

#### 分层约定
```
Controller  → 接收请求、参数校验、返回 ApiResult
Service     → 业务逻辑、事务管理
PO          → 数据库实体（@Entity, @Table）
DTO         → 请求参数对象（AddDTO, UpdateDTO, PageDTO）
VO          → 响应视图对象
```

#### API 端点规范
- URL 路径采用 `kebab-case`（短横线命名）
- 查询用 `GET`，修改用 `POST`
- **少于 3 个参数用 `@Param` + 路径变量，避免 `@Body`**
  ```java
  // ✅ 正确
  @Post @Mapping("/delete/{id}")
  public ApiResult<?> delete(@Param("id") BigInteger id)

  // ✅ 多个参数用路径
  @Post @Mapping("/truncate/{convId}/{msgId}")
  public ApiResult<?> truncate(@Param("convId") BigInteger convId, @Param("msgId") BigInteger msgId)

  // ❌ 避免（除非参数 >= 3 个或为复杂对象）
  @Post @Mapping("/delete")
  public ApiResult<?> delete(@Body BigInteger id)
  ```
- 新增 `/ai` 前缀的端点统一在 `cyrene-modules/cyreneai-api` 中

#### 数据库操作
- 使用 Sqltoy `LightDao`，**不直接使用 JPA/Hibernate**
- 实体类用 `@Entity` + `@Column` 注解，主键用雪花 ID
- 所有表要有 `create_time`、`is_deleted`（软删除）
- **新增字段必须同步更新 `scripts/ddl.sql` 和执行 `ALTER TABLE`**

#### AI 模型扩展
- 新增 `providerType` 时在 `ChatController.buildStreamingChatModel()` 的 `switch` 中添加分支
- 新增的 `ChatMessage` 转换逻辑在 `convertMessages()` 中统一处理
- 模型参数（temperature、topP 等）通过 `ChatRequestDTO` 透传

### 前端

#### 目录约定
```
src/
├── api/            # API 接口（按后端模块分文件）
├── views/          # 页面组件（按后端模块分目录）
├── composables/    # 可组合逻辑（如 useChatStream）
├── stores/         # Pinia 状态管理
├── components/     # 通用组件
└── router/         # 路由配置
```

#### API 调用
- 统一使用 `src/utils/axios-util.ts` 的 `request()` 函数
- POST 请求传空 body 时传 `{}`
- **路径参数嵌入 URL 字符串模板**

  ```typescript
  // ✅ 正确
  export function deleteConversation(id: string) {
    return request(`ai/conversation/delete/${id}`, {}, 'POST')
  }

  // ❌ 避免（路径参数通过 body 传递）
  export function deleteConversation(id: string) {
    return request('ai/conversation/delete', {id}, 'POST')
  }
  ```

#### 组件规范
- 使用 `<script setup lang="ts">`
- 优先使用 Element Plus 组件
- 样式使用 `<style scoped>`
- 新页面需要在 `router/modules/routes.ts` 注册路由

---

## Git 提交规范

使用 [git-cliff](https://git-cliff.org) 自动生成 CHANGELOG，提交信息遵循 Conventional Commits：

```
<type>: <description>

feat:      新功能
fix:       修复 bug
refactor:  重构
perf:      性能优化
docs:      文档
style:     代码格式
chore:     构建/工具
```

示例：
```
feat: add message edit/delete functionality
fix: use @Param instead of @Body for single-param endpoints
refactor: extract streaming model builder to factory
```

---

## 数据库迁移

每次新增表或字段需更新：

1. **`scripts/ddl.sql`** — 完整建表 DDL（用于全新部署）
2. **执行 `ALTER TABLE`** — 开发/生产环境增量迁移
3. **`scripts/init-data.sql`** — 初始化数据（如需要）

---

## 调试与测试

- 前端通过 `F12` 查看网络请求和 Console 日志
- 后端日志级别在 `app.yml` 中调整
- Sa-Token 相关错误检查 Redis 连接和 Token 传递
- AI 流式响应问题检查 `useChatStream.ts` 的 SSE 解析逻辑
