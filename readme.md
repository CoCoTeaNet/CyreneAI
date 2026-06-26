# CyreneAI

基于 Solon + Vue3 的 AI 平台，Admin 底座来自 [CyreneAdmin](https://gitee.com/momoljw/CyreneAdmin)。

## 项目结构

```
CyreneAI/
├── cyrene-modules/           # ⭐ 业务模块（主要开发区域）
│   ├── cyreneai-api/         # AI 接口定义（DTO、枚举、常量）
│   ├── cyreneai-biz/         # AI 业务实现（Service、Controller）
│   └── cyreneai-core/        # AI 核心能力（模型调用、工具封装）
│
├── cyrene-common/            # 公共模块（工具类、通用组件）
├── cyrene-service-system/    # 系统服务模块（用户、角色、菜单等基础功能）
├── cyrene-starter-solon/     # Solon 启动器配置
├── cyrene-ui/                # 前端 Vue3 项目
├── doc-api/                  # API 文档
├── doc-img/                  # 文档图片
├── scripts/                  # 脚本工具
└── pom.xml                   # Maven 父工程
```

## 技术栈

- **后端**: Solon + Sqltoy + Sa-Token
- **前端**: Vue3 + TypeScript + Element Plus
- **数据库**: MySQL/PostgreSQL + Redis
- **AI**: LangChain4j + 多模态模型支持
