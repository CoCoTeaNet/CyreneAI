-- 如果数据库不存在则创建
CREATE DATABASE IF NOT EXISTS cyrene_ai CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE cyrene_ai;

-- cyrene_ai.sys_dictionary definition

CREATE TABLE `sys_dictionary` (
  `id` bigint(20) NOT NULL COMMENT '字典id',
  `parent_id` bigint(20) DEFAULT NULL COMMENT '父级id',
  `dictionary_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '字典名称',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `sort` int(11) NOT NULL COMMENT '排序号',
  `enable_status` tinyint(4) DEFAULT '1' COMMENT '启用状态;0关闭 1启用',
  `create_by` bigint(20) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint(20) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除',
  `revision` int(11) DEFAULT NULL COMMENT '乐观锁',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='系统字典表';


-- cyrene_ai.sys_log definition

CREATE TABLE `sys_log` (
  `id` bigint(20) NOT NULL COMMENT '日志编号',
  `ip_address` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '请求ip地址',
  `operator` bigint(20) DEFAULT NULL COMMENT '操作人员',
  `request_way` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '请求方式',
  `log_status` tinyint(4) DEFAULT NULL COMMENT '日志状态;0异常 1成功',
  `log_type` tinyint(4) DEFAULT NULL COMMENT '日志类型：1登录 2操作 ',
  `api_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '接口请求路径',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `sys_log_request_way_index` (`request_way`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='系统操作日志表';


-- cyrene_ai.sys_menu definition

CREATE TABLE `sys_menu` (
  `id` bigint(20) NOT NULL COMMENT '菜单id',
  `menu_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜单名称',
  `permission_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '权限编号',
  `router_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '路由地址',
  `parent_id` bigint(20) DEFAULT NULL COMMENT '父级id',
  `menu_type` tinyint(4) DEFAULT NULL COMMENT '按钮类型;0目录 1菜单 2按钮',
  `is_menu` tinyint(4) DEFAULT NULL COMMENT '是否菜单',
  `menu_status` tinyint(4) DEFAULT '0' COMMENT '菜单状态：0显示 1隐藏',
  `component_path` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '组件路径',
  `is_external_link` tinyint(4) DEFAULT '0' COMMENT '是否外链',
  `icon_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '菜单图标',
  `sort` int(11) DEFAULT NULL COMMENT '显示顺序',
  `create_by` bigint(20) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint(20) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除',
  `revision` int(11) DEFAULT NULL COMMENT '乐观锁',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='系统菜单表';


-- cyrene_ai.sys_role definition

CREATE TABLE `sys_role` (
  `id` bigint(20) NOT NULL COMMENT '角色id',
  `role_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名称',
  `role_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色标识',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `sort` int(11) DEFAULT NULL COMMENT '显示排序',
  `create_by` bigint(20) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint(20) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除',
  `revision` int(11) DEFAULT NULL COMMENT '乐观锁',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='系统角色表';


-- cyrene_ai.sys_role_menu definition

CREATE TABLE `sys_role_menu` (
  `id` bigint(20) NOT NULL COMMENT '角色菜单关联id',
  `role_id` bigint(20) NOT NULL COMMENT '角色id',
  `menu_id` bigint(20) NOT NULL COMMENT '菜单id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='角色菜单关联表';


-- cyrene_ai.sys_user definition

CREATE TABLE `sys_user` (
  `id` bigint(20) NOT NULL COMMENT '用户id',
  `username` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录账号',
  `nickname` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户昵称',
  `password` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `sex` tinyint(4) NOT NULL COMMENT '用户性别;0未知 1男 2女',
  `email` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户邮箱',
  `mobile_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号',
  `account_status` tinyint(4) NOT NULL COMMENT '账号状态;0停用 1正常 2冻结 3封禁',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像地址',
  `last_login_ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最后登录ip',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `create_by` bigint(20) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint(20) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除',
  `revision` int(11) DEFAULT NULL COMMENT '乐观锁',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='系统用户表';


-- cyrene_ai.sys_user_role definition

CREATE TABLE `sys_user_role` (
  `id` bigint(20) NOT NULL COMMENT '用户角色关联id',
  `user_id` bigint(20) NOT NULL COMMENT '用户id',
  `role_id` bigint(20) NOT NULL COMMENT '角色id',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='用户角色关联表';


-- cyrene_ai.ai_model_provider definition

CREATE TABLE `ai_model_provider` (
  `id` bigint(20) NOT NULL COMMENT '提供商id',
  `provider_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '提供商名称',
  `provider_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '提供商类型;openai, anthropic, dashscope, ollama 等',
  `api_base_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'API 地址',
  `api_key` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'API 密钥',
  `sort` int(11) DEFAULT '0' COMMENT '排序号',
  `enable_status` tinyint(4) DEFAULT '1' COMMENT '启用状态;0关闭 1启用',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_by` bigint(20) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint(20) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除',
  `revision` int(11) DEFAULT NULL COMMENT '乐观锁',
  PRIMARY KEY (`id`),
  KEY `ai_model_provider_type_index` (`provider_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='AI 模型提供商表';


-- cyrene_ai.ai_model definition

CREATE TABLE `ai_model` (
  `id` bigint(20) NOT NULL COMMENT '模型id',
  `provider_id` bigint(20) NOT NULL COMMENT '提供商id',
  `model_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模型名称',
  `context_window` int(11) DEFAULT NULL COMMENT '上下文窗口大小',
  `input_price` decimal(10,4) DEFAULT NULL COMMENT '输入价格(每千token)',
  `output_price` decimal(10,4) DEFAULT NULL COMMENT '输出价格(每千token)',
  `is_default` tinyint(4) DEFAULT '0' COMMENT '是否默认;0否 1是',
  `sort` int(11) DEFAULT '0' COMMENT '排序号',
  `enable_status` tinyint(4) DEFAULT '1' COMMENT '启用状态;0关闭 1启用',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `create_by` bigint(20) NOT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_by` bigint(20) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `is_deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除',
  `revision` int(11) DEFAULT NULL COMMENT '乐观锁',
  PRIMARY KEY (`id`),
  KEY `ai_model_provider_id_index` (`provider_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='AI 模型表';

-- AI对话表
CREATE TABLE IF NOT EXISTS `ai_conversation` (
                                                 `id` BIGINT NOT NULL COMMENT '主键ID',
                                                 `title` VARCHAR(200) DEFAULT NULL COMMENT '对话标题',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `model_id` BIGINT DEFAULT NULL COMMENT '模型ID',
    `system_prompt` TEXT DEFAULT NULL COMMENT '系统提示词',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除 0:否 1:是',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_created_time` (`created_time`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI对话表';

-- AI消息表
CREATE TABLE IF NOT EXISTS `ai_message` (
                                            `id` BIGINT NOT NULL COMMENT '主键ID',
                                            `conversation_id` BIGINT NOT NULL COMMENT '对话ID',
                                            `role` VARCHAR(20) NOT NULL COMMENT '角色: user/assistant/system',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `prompt_tokens` INT DEFAULT 0 COMMENT '输入token数',
    `completion_tokens` INT DEFAULT 0 COMMENT '输出token数',
    `total_tokens` INT DEFAULT 0 COMMENT '总token数',
    `cost` DECIMAL(12,6) DEFAULT 0 COMMENT '本次花费(元)',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_conversation_id` (`conversation_id`),
    INDEX `idx_created_time` (`created_time`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI消息表';

-- 嵌入模型配置表
CREATE TABLE IF NOT EXISTS `ai_embedding_model` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `provider_type` VARCHAR(50) NOT NULL COMMENT '提供商类型;dashscope, openai',
    `model_name` VARCHAR(100) NOT NULL COMMENT '模型名称;如 text-embedding-v3, text-embedding-3-small',
    `api_key` VARCHAR(512) DEFAULT NULL COMMENT 'API密钥',
    `api_base_url` VARCHAR(255) DEFAULT NULL COMMENT 'API地址',
    `dimension` INT DEFAULT 1024 COMMENT '向量维度',
    `is_default` TINYINT DEFAULT 0 COMMENT '是否默认;0否 1是',
    `enable_status` TINYINT DEFAULT 1 COMMENT '启用状态;0关闭 1启用',
    `sort` INT DEFAULT 0 COMMENT '排序号',
    `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
    `create_by` BIGINT NOT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
    `revision` INT DEFAULT NULL COMMENT '乐观锁',
    PRIMARY KEY (`id`),
    INDEX `idx_provider_type` (`provider_type`),
    INDEX `idx_is_default` (`is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='嵌入模型配置表';

-- 文档表
CREATE TABLE IF NOT EXISTS `ai_document` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `name` VARCHAR(255) NOT NULL COMMENT '文档名称',
    `type` VARCHAR(20) NOT NULL COMMENT '文档类型;pdf, docx, txt, md',
    `size` BIGINT DEFAULT 0 COMMENT '文件大小(字节)',
    `file_path` VARCHAR(512) DEFAULT NULL COMMENT '文件存储路径',
    `status` TINYINT DEFAULT 0 COMMENT '处理状态;0待处理 1处理中 2已完成 3失败',
    `chunk_count` INT DEFAULT 0 COMMENT '分块数量',
    `chunk_strategy` VARCHAR(50) DEFAULT 'paragraph' COMMENT '分块策略;size, paragraph, recursive',
    `chunk_size` INT DEFAULT 500 COMMENT '分块大小',
    `chunk_overlap` INT DEFAULT 50 COMMENT '分块重叠',
    `error_msg` TEXT DEFAULT NULL COMMENT '错误信息',
    `kb_id` BIGINT DEFAULT NULL COMMENT '所属知识库ID',
    `create_by` BIGINT NOT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
    `revision` INT DEFAULT NULL COMMENT '乐观锁',
    PRIMARY KEY (`id`),
    INDEX `idx_kb_id` (`kb_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文档表';

-- ai_document_chunk 已迁移至 PostgreSQL (pgvector)，详见 scripts/ddl-pgvector.sql

-- 知识库表
CREATE TABLE IF NOT EXISTS `ai_knowledge_base` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `name` VARCHAR(200) NOT NULL COMMENT '知识库名称',
    `description` TEXT DEFAULT NULL COMMENT '知识库描述',
    `model_id` BIGINT DEFAULT NULL COMMENT '关联模型ID(用于RAG回答)',
    `embedding_model_id` BIGINT DEFAULT NULL COMMENT '嵌入模型ID',
    `chunk_size` INT DEFAULT 500 COMMENT '默认分块大小',
    `chunk_overlap` INT DEFAULT 50 COMMENT '默认分块重叠',
    `chunk_strategy` VARCHAR(50) DEFAULT 'paragraph' COMMENT '默认分块策略',
    `retrieval_strategy` VARCHAR(50) DEFAULT 'top_k' COMMENT '检索策略;top_k, mmr, hybrid',
    `top_k` INT DEFAULT 5 COMMENT '检索返回条数',
    `similarity_threshold` DECIMAL(5,4) DEFAULT 0.7 COMMENT '相似度阈值',
    `enable_status` TINYINT DEFAULT 1 COMMENT '启用状态;0关闭 1启用',
    `create_by` BIGINT NOT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
    `revision` INT DEFAULT NULL COMMENT '乐观锁',
    PRIMARY KEY (`id`),
    INDEX `idx_enable_status` (`enable_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='知识库表';

-- 知识库文档关联表
CREATE TABLE IF NOT EXISTS `ai_kb_document` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `kb_id` BIGINT NOT NULL COMMENT '知识库ID',
    `document_id` BIGINT NOT NULL COMMENT '文档ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_kb_doc` (`kb_id`, `document_id`),
    INDEX `idx_kb_id` (`kb_id`),
    INDEX `idx_document_id` (`document_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='知识库文档关联表';

-- ============================================================
-- Phase 4: Agent / Tool Calling
-- ============================================================

-- AI工具表
CREATE TABLE IF NOT EXISTS `ai_tool` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '工具名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '工具描述',
    `type` VARCHAR(20) NOT NULL COMMENT '工具类型;builtin, custom',
    `schema_json` TEXT DEFAULT NULL COMMENT '参数JSON Schema',
    `url` VARCHAR(500) DEFAULT NULL COMMENT '自定义工具URL',
    `auth_type` VARCHAR(20) DEFAULT NULL COMMENT '认证类型;none, bearer, basic',
    `auth_value` VARCHAR(500) DEFAULT NULL COMMENT '认证值',
    `http_method` VARCHAR(10) DEFAULT 'POST' COMMENT 'HTTP方法;GET, POST',
    `builtin_handler` VARCHAR(50) DEFAULT NULL COMMENT '内置工具处理器标识;calculator,datetime,websearch,knowledgebase,codeexecution,imagegen,imagerec,weather',
    `enable_status` TINYINT DEFAULT 1 COMMENT '启用状态;0关闭 1启用',
    `sort` INT DEFAULT 0 COMMENT '排序号',
    `create_by` BIGINT NOT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
    `revision` INT DEFAULT NULL COMMENT '乐观锁',
    PRIMARY KEY (`id`),
    INDEX `idx_type` (`type`),
    INDEX `idx_builtin_handler` (`builtin_handler`),
    INDEX `idx_enable_status` (`enable_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI工具表';

-- AI智能体表
CREATE TABLE IF NOT EXISTS `ai_agent` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '智能体名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '智能体描述',
    `model_id` BIGINT DEFAULT NULL COMMENT '关联模型ID',
    `system_prompt` TEXT DEFAULT NULL COMMENT '系统提示词',
    `tool_ids` TEXT DEFAULT NULL COMMENT '关联工具ID列表(JSON数组)',
    `max_iterations` INT DEFAULT 10 COMMENT '最大迭代次数',
    `temperature` DECIMAL(3,2) DEFAULT 0.7 COMMENT '温度参数',
    `top_p` DECIMAL(3,2) DEFAULT 0.9 COMMENT 'Top-P参数',
    `max_tokens` INT DEFAULT 2048 COMMENT '最大输出token数',
    `enable_status` TINYINT DEFAULT 1 COMMENT '启用状态;0关闭 1启用',
    `sort` INT DEFAULT 0 COMMENT '排序号',
    `create_by` BIGINT NOT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
    `revision` INT DEFAULT NULL COMMENT '乐观锁',
    PRIMARY KEY (`id`),
    INDEX `idx_enable_status` (`enable_status`),
    INDEX `idx_model_id` (`model_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI智能体表';

-- AI智能体运行日志表
CREATE TABLE IF NOT EXISTS `ai_agent_log` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `agent_id` BIGINT DEFAULT NULL COMMENT '智能体ID',
    `agent_name` VARCHAR(100) DEFAULT NULL COMMENT '智能体名称',
    `conversation_id` BIGINT DEFAULT NULL COMMENT '对话ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `user_input` TEXT DEFAULT NULL COMMENT '用户输入',
    `final_response` TEXT DEFAULT NULL COMMENT '最终回复',
    `iteration_count` INT DEFAULT 0 COMMENT '迭代次数',
    `tool_calls` TEXT DEFAULT NULL COMMENT '工具调用记录(JSON)',
    `prompt_tokens` INT DEFAULT 0 COMMENT '输入token数',
    `completion_tokens` INT DEFAULT 0 COMMENT '输出token数',
    `total_tokens` INT DEFAULT 0 COMMENT '总token数',
    `cost` DECIMAL(12,6) DEFAULT 0 COMMENT '总花费(元)',
    `status` VARCHAR(20) DEFAULT 'success' COMMENT '运行状态;success, error, timeout',
    `error_msg` TEXT DEFAULT NULL COMMENT '错误信息',
    `execution_time_ms` BIGINT DEFAULT 0 COMMENT '执行耗时(毫秒)',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_agent_id` (`agent_id`),
    INDEX `idx_conversation_id` (`conversation_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_created_time` (`created_time`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI智能体运行日志表';
