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
        ]
    },
    {
        path: '/:pathMatch(.*)',
        name: 'error',
        component: NotFound,
        meta: {title: '404'},
    }
];
