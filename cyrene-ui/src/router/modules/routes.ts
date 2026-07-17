const AdminLayout = () => import("@/layout/AdminLayout.vue");
const Home = () => import("@/views/system/dashboard/Home.vue");
const UserView = () => import("@/views/system/manager/system/user/UserView.vue");
const MenuView = () => import("@/views/system/manager/system/menu/MenuView.vue");
const DictionaryView = () => import("@/views/system/manager/system/dictionary/DictionaryView.vue");
const PermissionView = () => import("@/views/system/manager/system/menu/PermissionView.vue");
const RoleView = () => import("@/views/system/manager/system/role/RoleView.vue");
const Dashboard = () => import("@/views/system/dashboard/Dashboard.vue");
const NotFound = () => import("@/views/error/NotFound.vue");
const UserCenterView = () => import("@/views/system/personal/UserCenterView.vue");
const OperationLogView = () => import("@/views/system/manager/system/log/SysLogView.vue");
const Login = () => import("@/views/system/login/Login.vue");
const AiModelProviderView = () => import("@/views/ai/model-provider/index.vue");
const AiModelView = () => import("@/views/ai/model/index.vue");
const AiChatView = () => import("@/views/ai/chat/index.vue");
const AiDocumentView = () => import("@/views/ai/document/index.vue");
const AiKnowledgeBaseView = () => import("@/views/ai/knowledge-base/index.vue");
const AiEmbeddingModelView = () => import("@/views/ai/embedding-model/index.vue");
const AiToolView = () => import("@/views/ai/tool/index.vue");
const AiAgentView = () => import("@/views/ai/agent/index.vue");
const AgentChatView = () => import("@/views/ai/agent-chat/index.vue");
const AiImageView = () => import("@/views/ai/image/index.vue");
const AiVisionModelView = () => import("@/views/ai/vision-model/index.vue");
const AiSttView = () => import("@/views/ai/stt/index.vue");
const AiTtsView = () => import("@/views/ai/tts/index.vue");
const AiPromptTemplateView = () => import("@/views/ai/prompt-template/index.vue");
const AiPromptPresetView = () => import("@/views/ai/prompt-preset/index.vue");
const AiPromptAbTestView = () => import("@/views/ai/prompt-ab-test/index.vue");
const AiPromptEvalView = () => import("@/views/ai/prompt-eval/index.vue");
const AiApiKeyView = () => import("@/views/ai/api-key/index.vue");
const AiQuotaAlertView = () => import("@/views/ai/quota-alert/index.vue");
const AiAuditLogView = () => import("@/views/ai/audit-log/index.vue");
const AiSensitiveWordView = () => import("@/views/ai/sensitive-word/index.vue");
const AiModerationRuleView = () => import("@/views/ai/moderation-rule/index.vue");
// Phase 8: 监控与观测
const AiMonitorView = () => import("@/views/ai/monitor/index.vue");
const AiBudgetView = () => import("@/views/ai/budget/index.vue");
const AiPlaygroundView = () => import("@/views/ai/playground/index.vue");
const AiEvalDatasetView = () => import("@/views/ai/eval-dataset/index.vue");


export const routes = [
    {
        path: "/login",
        name: "Login",
        meta: {title: 'Welcome to Admin'},
        component: Login
    },
    {
        path: '/admin',
        name: 'Admin',
        meta: {title: '后台管理'},
        component: AdminLayout,
        redirect: {name: 'Home'},
        children: [
            // 其它模块
            {path: 'home', meta: {title: '首页'}, name: 'Home', component: Home},
            {path: 'dashboard', meta: {title: '仪表盘'}, name: 'Dashboard', component: Dashboard},
            // 系统模块
            {path: 'sys-user-manager', meta: {title: '用户管理'}, name: 'UserView', component: UserView},
            {path: 'sys-menu-manager', meta: {title: '菜单管理'}, name: 'MenuView', component: MenuView},
            {path: 'sys-permission-manager', meta: {title: '权限管理'}, name: 'PermissionView', component: PermissionView},
            {path: 'sys-role-manager', meta: {title: '角色管理'}, name: 'RoleView', component: RoleView},
            {path: 'sys-dictionary-manager', meta: {title: '字典管理'}, name: 'DictionaryView', component: DictionaryView},
            {path: 'sys-log-manager', meta: {title: '日志管理'}, name: 'OperationLogView', component: OperationLogView},
            {path: 'sys-user-center', meta: {title: '用户中心'}, name: 'UserCenterView', component: UserCenterView},
            // AI 模块
            {path: 'ai-chat', meta: {title: 'AI Chat'}, name: 'AiChatView', component: AiChatView},
            {path: 'ai-model-provider', meta: {title: '模型提供商'}, name: 'AiModelProviderView', component: AiModelProviderView},
            {path: 'ai-model', meta: {title: '模型管理'}, name: 'AiModelView', component: AiModelView},
            {path: 'ai-document', meta: {title: '文档管理'}, name: 'AiDocumentView', component: AiDocumentView},
            {path: 'ai-knowledge-base', meta: {title: '知识库'}, name: 'AiKnowledgeBaseView', component: AiKnowledgeBaseView},
            {path: 'ai-embedding-model', meta: {title: '嵌入模型'}, name: 'AiEmbeddingModelView', component: AiEmbeddingModelView},
            {path: 'ai-tool', meta: {title: '工具管理'}, name: 'AiToolView', component: AiToolView},
            {path: 'ai-agent', meta: {title: '智能体'}, name: 'AiAgentView', component: AiAgentView},
            {path: 'ai-agent-chat', meta: {title: '智能体对话'}, name: 'AgentChatView', component: AgentChatView},
            {path: 'ai-image', meta: {title: '图片生成'}, name: 'AiImageView', component: AiImageView},
            {path: 'ai-vision-model', meta: {title: '视觉模型'}, name: 'AiVisionModelView', component: AiVisionModelView},
            {path: 'ai-stt', meta: {title: '语音转文字'}, name: 'AiSttView', component: AiSttView},
            {path: 'ai-tts', meta: {title: '文本转语音'}, name: 'AiTtsView', component: AiTtsView},
            {path: 'ai-prompt-template', meta: {title: '提示词模板'}, name: 'AiPromptTemplateView', component: AiPromptTemplateView},
            {path: 'ai-prompt-preset', meta: {title: '预设提示词'}, name: 'AiPromptPresetView', component: AiPromptPresetView},
            {path: 'ai-prompt-ab-test', meta: {title: 'A/B 测试'}, name: 'AiPromptAbTestView', component: AiPromptAbTestView},
            {path: 'ai-prompt-eval', meta: {title: 'Prompt 评估'}, name: 'AiPromptEvalView', component: AiPromptEvalView},
            // Phase 7: 管理与治理
            {path: 'ai-api-key', meta: {title: 'API Key 管理'}, name: 'AiApiKeyView', component: AiApiKeyView},
            {path: 'ai-quota-alert', meta: {title: '配额告警'}, name: 'AiQuotaAlertView', component: AiQuotaAlertView},
            {path: 'ai-audit-log', meta: {title: '审计日志'}, name: 'AiAuditLogView', component: AiAuditLogView},
            {path: 'ai-sensitive-word', meta: {title: '敏感词管理'}, name: 'AiSensitiveWordView', component: AiSensitiveWordView},
            {path: 'ai-moderation-rule', meta: {title: '内容审核规则'}, name: 'AiModerationRuleView', component: AiModerationRuleView},
            // Phase 8: 监控与观测
            {path: 'ai-monitor', meta: {title: '监控面板'}, name: 'AiMonitorView', component: AiMonitorView},
            {path: 'ai-budget', meta: {title: '成本预算'}, name: 'AiBudgetView', component: AiBudgetView},
            {path: 'ai-playground', meta: {title: '模型 Playground'}, name: 'AiPlaygroundView', component: AiPlaygroundView},
            {path: 'ai-eval-dataset', meta: {title: '评估数据集'}, name: 'AiEvalDatasetView', component: AiEvalDatasetView},
        ]
    },
    {
        path: '/:pathMatch(.*)',
        name: 'error',
        component: NotFound,
        meta: {title: '404'},
    }
];
