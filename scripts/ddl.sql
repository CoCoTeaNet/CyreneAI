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


-- cyrene_ai.ai_model definition (unified model table)

CREATE TABLE `ai_model` (
  `id` bigint(20) NOT NULL COMMENT '模型id',
  `model_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模型类型;chat, image, vision, tts, stt, embedding',
  `provider_id` bigint(20) NOT NULL COMMENT '提供商id',
  `model_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模型名称',
  `context_window` int(11) DEFAULT NULL COMMENT '上下文窗口大小(仅chat模型)',
  `input_price` decimal(10,4) DEFAULT NULL COMMENT '输入价格(每千token, 仅chat模型)',
  `output_price` decimal(10,4) DEFAULT NULL COMMENT '输出价格(每千token, 仅chat模型)',
  `dimension` int(11) DEFAULT NULL COMMENT '向量维度(仅embedding模型)',
  `default_size` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '默认图片尺寸(仅image模型)',
  `default_voice` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '默认音色(仅tts模型)',
  `default_system_prompt` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '模型默认系统提示词(仅chat/vision模型)',
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
  KEY `ai_model_type_index` (`model_type`),
  KEY `ai_model_provider_id_index` (`provider_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC COMMENT='AI 统一模型表';

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

-- ============================================================
-- Phase 5: 多模态
-- ============================================================

-- AI图片生成记录表
CREATE TABLE IF NOT EXISTS `ai_image_record` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `prompt` TEXT NOT NULL COMMENT '生成提示词',
    `revised_prompt` TEXT DEFAULT NULL COMMENT '优化后的提示词',
    `model_name` VARCHAR(100) DEFAULT NULL COMMENT '使用的模型',
    `image_url` TEXT DEFAULT NULL COMMENT '图片URL',
    `image_size` VARCHAR(20) DEFAULT '1024x1024' COMMENT '图片尺寸',
    `style` VARCHAR(50) DEFAULT NULL COMMENT '图片风格',
    `cost` DECIMAL(12,6) DEFAULT 0 COMMENT '花费(元)',
    `create_by` BIGINT NOT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
    `revision` INT DEFAULT NULL COMMENT '乐观锁',
    PRIMARY KEY (`id`),
    INDEX `idx_create_by` (`create_by`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI图片生成记录表';

-- AI TTS 生成记录表
CREATE TABLE IF NOT EXISTS `ai_tts_record` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `text` TEXT NOT NULL COMMENT '合成文本',
    `model_name` VARCHAR(100) DEFAULT NULL COMMENT '使用的模型',
    `voice` VARCHAR(50) DEFAULT NULL COMMENT '音色',
    `audio_url` VARCHAR(500) DEFAULT NULL COMMENT '音频文件URL',
    `file_size` BIGINT DEFAULT 0 COMMENT '文件大小(字节)',
    `duration_seconds` DECIMAL(10,2) DEFAULT NULL COMMENT '音频时长(秒)',
    `cost` DECIMAL(12,6) DEFAULT 0 COMMENT '花费(元)',
    `create_by` BIGINT NOT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
    `revision` INT DEFAULT NULL COMMENT '乐观锁',
    PRIMARY KEY (`id`),
    INDEX `idx_create_by` (`create_by`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI TTS生成记录表';

-- AI STT 识别记录表
CREATE TABLE IF NOT EXISTS `ai_stt_record` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `audio_url` VARCHAR(500) DEFAULT NULL COMMENT '音频文件URL',
    `model_name` VARCHAR(100) DEFAULT NULL COMMENT '使用的模型',
    `transcript` TEXT DEFAULT NULL COMMENT '识别文本',
    `file_size` BIGINT DEFAULT 0 COMMENT '文件大小(字节)',
    `duration_seconds` DECIMAL(10,2) DEFAULT NULL COMMENT '音频时长(秒)',
    `cost` DECIMAL(12,6) DEFAULT 0 COMMENT '花费(元)',
    `create_by` BIGINT NOT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
    `revision` INT DEFAULT NULL COMMENT '乐观锁',
    PRIMARY KEY (`id`),
    INDEX `idx_create_by` (`create_by`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI STT识别记录表';

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

-- ============================================================
-- Phase 6: Prompt 管理
-- ============================================================

-- AI 提示词模板表
CREATE TABLE IF NOT EXISTS `ai_prompt_template` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `name` VARCHAR(200) NOT NULL COMMENT '模板名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '模板描述',
    `category` VARCHAR(50) DEFAULT NULL COMMENT '分类;general, translation, code, writing, roleplay, custom 等',
    `scene` VARCHAR(20) DEFAULT 'system' COMMENT '适用场景;system, user, mixed',
    `content` TEXT NOT NULL COMMENT '模板内容(支持 {{variable}} 变量)',
    `variables` TEXT DEFAULT NULL COMMENT '变量列表(JSON 数组: [{"name":"xx","label":"xx","default":"xx"}])',
    `current_version` INT DEFAULT 1 COMMENT '当前版本号',
    `enable_status` TINYINT DEFAULT 1 COMMENT '启用状态;0关闭 1启用',
    `sort` INT DEFAULT 0 COMMENT '排序号',
    `create_by` BIGINT NOT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
    `revision` INT DEFAULT NULL COMMENT '乐观锁',
    PRIMARY KEY (`id`),
    INDEX `idx_category` (`category`),
    INDEX `idx_enable_status` (`enable_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI提示词模板表';

-- AI 系统提示词预设表(用于对话快速选择)
CREATE TABLE IF NOT EXISTS `ai_prompt_preset` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `name` VARCHAR(200) NOT NULL COMMENT '预设名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '预设描述',
    `category` VARCHAR(50) DEFAULT NULL COMMENT '分类',
    `content` TEXT NOT NULL COMMENT '提示词内容',
    `icon` VARCHAR(50) DEFAULT NULL COMMENT '图标',
    `is_builtin` TINYINT DEFAULT 0 COMMENT '是否内置;0否 1是(内置不可删除)',
    `enable_status` TINYINT DEFAULT 1 COMMENT '启用状态;0关闭 1启用',
    `sort` INT DEFAULT 0 COMMENT '排序号',
    `create_by` BIGINT NOT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
    `revision` INT DEFAULT NULL COMMENT '乐观锁',
    PRIMARY KEY (`id`),
    INDEX `idx_category` (`category`),
    INDEX `idx_enable_status` (`enable_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI系统提示词预设表';

-- AI 提示词模板版本历史表
CREATE TABLE IF NOT EXISTS `ai_prompt_template_version` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `template_id` BIGINT NOT NULL COMMENT '模板ID',
    `version` INT NOT NULL COMMENT '版本号(从1递增)',
    `content` TEXT NOT NULL COMMENT '当次版本内容',
    `variables` TEXT DEFAULT NULL COMMENT '变量列表(JSON)',
    `change_note` VARCHAR(500) DEFAULT NULL COMMENT '变更说明',
    `create_by` BIGINT NOT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_template_version` (`template_id`, `version`),
    INDEX `idx_template_id` (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI提示词模板版本历史表';

-- AI 提示词 A/B 测试表
CREATE TABLE IF NOT EXISTS `ai_prompt_ab_test` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `name` VARCHAR(200) NOT NULL COMMENT '测试名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '测试描述',
    `template_a_id` BIGINT NOT NULL COMMENT '版本A模板ID',
    `template_a_version` INT DEFAULT NULL COMMENT '版本A使用的版本号(为空则最新)',
    `template_b_id` BIGINT NOT NULL COMMENT '版本B模板ID',
    `template_b_version` INT DEFAULT NULL COMMENT '版本B使用的版本号(为空则最新)',
    `model_id` BIGINT DEFAULT NULL COMMENT '使用的模型ID',
    `traffic_split` INT DEFAULT 50 COMMENT '流量分配百分比(A侧, 0-100)',
    `status` VARCHAR(20) DEFAULT 'running' COMMENT '状态;draft, running, finished',
    `create_by` BIGINT NOT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
    `revision` INT DEFAULT NULL COMMENT '乐观锁',
    PRIMARY KEY (`id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI提示词A/B测试表';

-- AI 提示词效果评估记录表
CREATE TABLE IF NOT EXISTS `ai_prompt_eval` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `template_id` BIGINT DEFAULT NULL COMMENT '模板ID',
    `template_version` INT DEFAULT NULL COMMENT '模板版本号',
    `model_id` BIGINT DEFAULT NULL COMMENT '模型ID',
    `model_name` VARCHAR(100) DEFAULT NULL COMMENT '模型名称',
    `ab_test_id` BIGINT DEFAULT NULL COMMENT '关联A/B测试ID',
    `variant` VARCHAR(10) DEFAULT NULL COMMENT 'A/B测试分组;A, B',
    `input_variables` TEXT DEFAULT NULL COMMENT '输入变量(JSON)',
    `rendered_prompt` TEXT DEFAULT NULL COMMENT '渲染后的提示词',
    `output` TEXT DEFAULT NULL COMMENT '模型输出',
    `prompt_tokens` INT DEFAULT 0 COMMENT '输入token数',
    `completion_tokens` INT DEFAULT 0 COMMENT '输出token数',
    `total_tokens` INT DEFAULT 0 COMMENT '总token数',
    `cost` DECIMAL(12,6) DEFAULT 0 COMMENT '本次花费(元)',
    `latency_ms` BIGINT DEFAULT 0 COMMENT '执行耗时(毫秒)',
    `rating` TINYINT DEFAULT NULL COMMENT '效果评分(1-5)',
    `feedback` VARCHAR(500) DEFAULT NULL COMMENT '评价反馈',
    `create_by` BIGINT NOT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_template_id` (`template_id`),
    INDEX `idx_ab_test_id` (`ab_test_id`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI提示词效果评估记录表';

-- 增量迁移: 为 ai_model 新增 default_system_prompt 字段
-- ALTER TABLE `ai_model` ADD COLUMN `default_system_prompt` TEXT DEFAULT NULL COMMENT '模型默认系统提示词(仅chat/vision模型)' AFTER `default_voice`;

-- =============================================================
-- Phase 7 — 管理与治理
-- =============================================================

-- 7.1 API Key 管理 —— 用户级 API Key 生成
CREATE TABLE IF NOT EXISTS `ai_api_key` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '所属用户ID',
    `name` VARCHAR(100) NOT NULL COMMENT 'Key 名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `key_hash` VARCHAR(128) NOT NULL COMMENT 'Key 的 SHA-256 哈希(不存明文)',
    `key_prefix` VARCHAR(20) NOT NULL COMMENT 'Key 前缀(用于展示,如 sk-cyr-****)',
    `allowed_model_ids` VARCHAR(1000) DEFAULT NULL COMMENT '允许使用的模型ID列表(逗号分隔,空=全部)',
    `allowed_ip_list` VARCHAR(1000) DEFAULT NULL COMMENT '允许调用的IP白名单(逗号分隔,空=不限)',
    `rpm_limit` INT DEFAULT NULL COMMENT '每分钟请求数限制(null=不限)',
    `tpm_limit` INT DEFAULT NULL COMMENT '每分钟Token数限制(null=不限)',
    `monthly_token_quota` BIGINT DEFAULT NULL COMMENT '月度Token配额(null=不限)',
    `tokens_used_this_month` BIGINT DEFAULT 0 COMMENT '本月已用Token数',
    `quota_reset_time` DATETIME DEFAULT NULL COMMENT '配额下次重置时间',
    `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间(null=永久)',
    `last_used_time` DATETIME DEFAULT NULL COMMENT '最近使用时间',
    `enable_status` TINYINT DEFAULT 1 COMMENT '启用状态;0关闭 1启用',
    `sort` INT DEFAULT 0 COMMENT '排序号',
    `create_by` BIGINT NOT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
    `revision` INT DEFAULT NULL COMMENT '乐观锁',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_key_hash` (`key_hash`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_enable_status` (`enable_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI API Key 表';

-- 7.1 API Key 每日调用统计(供统计面板聚合)
CREATE TABLE IF NOT EXISTS `ai_api_key_usage_daily` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `api_key_id` BIGINT NOT NULL COMMENT 'API Key ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `stat_date` DATE NOT NULL COMMENT '统计日期',
    `request_count` INT NOT NULL DEFAULT 0 COMMENT '请求次数',
    `success_count` INT NOT NULL DEFAULT 0 COMMENT '成功次数',
    `blocked_count` INT NOT NULL DEFAULT 0 COMMENT '被拦截次数',
    `error_count` INT NOT NULL DEFAULT 0 COMMENT '异常次数',
    `prompt_tokens` BIGINT NOT NULL DEFAULT 0 COMMENT '输入Token数',
    `completion_tokens` BIGINT NOT NULL DEFAULT 0 COMMENT '输出Token数',
    `total_tokens` BIGINT NOT NULL DEFAULT 0 COMMENT '总Token数',
    `cost` DECIMAL(14,6) NOT NULL DEFAULT 0 COMMENT '当日花费(元)',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_key_date` (`api_key_id`, `stat_date`),
    INDEX `idx_user_date` (`user_id`, `stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI API Key 每日调用统计';

-- 7.2 配额告警
CREATE TABLE IF NOT EXISTS `ai_quota_alert` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '告警名称',
    `scope` VARCHAR(20) NOT NULL COMMENT '作用范围;global 全局, key 单Key',
    `api_key_id` BIGINT DEFAULT NULL COMMENT '关联API Key ID(scope=key 时必填)',
    `metric` VARCHAR(20) NOT NULL COMMENT '监控指标;monthly_tokens, daily_cost, error_rate',
    `threshold_percent` INT DEFAULT NULL COMMENT '阈值百分比(相对配额)',
    `threshold_value` DECIMAL(14,6) DEFAULT NULL COMMENT '阈值绝对值',
    `notify_channel` VARCHAR(50) DEFAULT 'system' COMMENT '通知渠道;system, email, webhook',
    `notify_target` VARCHAR(500) DEFAULT NULL COMMENT '通知目标(邮箱/URL)',
    `last_triggered_time` DATETIME DEFAULT NULL COMMENT '最近触发时间',
    `trigger_count` INT NOT NULL DEFAULT 0 COMMENT '累计触发次数',
    `enable_status` TINYINT DEFAULT 1 COMMENT '启用状态;0关闭 1启用',
    `create_by` BIGINT NOT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
    `revision` INT DEFAULT NULL COMMENT '乐观锁',
    PRIMARY KEY (`id`),
    INDEX `idx_scope` (`scope`),
    INDEX `idx_api_key_id` (`api_key_id`),
    INDEX `idx_enable_status` (`enable_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI 配额告警配置表';

-- 7.3 审计日志
CREATE TABLE IF NOT EXISTS `ai_audit_log` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
    `api_key_id` BIGINT DEFAULT NULL COMMENT 'API Key ID(通过Key调用时填充)',
    `endpoint` VARCHAR(200) NOT NULL COMMENT '端点路径',
    `http_method` VARCHAR(10) DEFAULT NULL COMMENT 'HTTP方法',
    `model_id` BIGINT DEFAULT NULL COMMENT '模型ID',
    `model_name` VARCHAR(100) DEFAULT NULL COMMENT '模型名称',
    `provider_type` VARCHAR(50) DEFAULT NULL COMMENT '提供商类型',
    `conversation_id` BIGINT DEFAULT NULL COMMENT '会话ID',
    `request_id` VARCHAR(64) DEFAULT NULL COMMENT '请求追踪ID',
    `prompt_snippet` VARCHAR(1000) DEFAULT NULL COMMENT '输入摘要(截断)',
    `output_snippet` VARCHAR(1000) DEFAULT NULL COMMENT '输出摘要(截断)',
    `prompt_tokens` INT DEFAULT 0 COMMENT '输入Token数',
    `completion_tokens` INT DEFAULT 0 COMMENT '输出Token数',
    `total_tokens` INT DEFAULT 0 COMMENT '总Token数',
    `cost` DECIMAL(12,6) DEFAULT 0 COMMENT '花费(元)',
    `latency_ms` BIGINT DEFAULT 0 COMMENT '耗时(毫秒)',
    `status` VARCHAR(20) NOT NULL COMMENT '状态;success, blocked, error, quota_exceeded, rate_limited',
    `error_msg` VARCHAR(1000) DEFAULT NULL COMMENT '错误信息',
    `ip` VARCHAR(64) DEFAULT NULL COMMENT '来源IP',
    `user_agent` VARCHAR(500) DEFAULT NULL COMMENT 'User-Agent',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_api_key_id` (`api_key_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI 请求审计日志表';

-- 7.4 敏感词
CREATE TABLE IF NOT EXISTS `ai_sensitive_word` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `word` VARCHAR(200) NOT NULL COMMENT '敏感词',
    `category` VARCHAR(50) DEFAULT 'custom' COMMENT '分类;politics, violence, adult, custom 等',
    `strategy` VARCHAR(20) NOT NULL DEFAULT 'block' COMMENT '策略;block 拦截, replace 替换, warn 警告',
    `replacement` VARCHAR(200) DEFAULT '***' COMMENT '替换文本(strategy=replace 生效)',
    `target` VARCHAR(20) NOT NULL DEFAULT 'both' COMMENT '作用目标;input 输入, output 输出, both 双向',
    `enable_status` TINYINT DEFAULT 1 COMMENT '启用状态;0关闭 1启用',
    `sort` INT DEFAULT 0 COMMENT '排序号',
    `create_by` BIGINT NOT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
    `revision` INT DEFAULT NULL COMMENT '乐观锁',
    PRIMARY KEY (`id`),
    INDEX `idx_word` (`word`),
    INDEX `idx_category` (`category`),
    INDEX `idx_enable_status` (`enable_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI 敏感词表';

-- 7.4 内容审核规则(接入 Moderation API 等)
CREATE TABLE IF NOT EXISTS `ai_moderation_rule` (
    `id` BIGINT NOT NULL COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '规则名称',
    `provider` VARCHAR(50) NOT NULL COMMENT '提供者;openai_moderation, dashscope, sensitive_word, keyword_regex',
    `config_json` TEXT DEFAULT NULL COMMENT '规则配置(JSON)',
    `threshold` DECIMAL(6,4) DEFAULT NULL COMMENT '分数阈值(0-1)',
    `action` VARCHAR(20) NOT NULL DEFAULT 'block' COMMENT '命中动作;block, replace, warn, pass',
    `target` VARCHAR(20) NOT NULL DEFAULT 'both' COMMENT '作用目标;input, output, both',
    `sort` INT DEFAULT 0 COMMENT '排序号(优先级,数值大优先)',
    `enable_status` TINYINT DEFAULT 1 COMMENT '启用状态;0关闭 1启用',
    `create_by` BIGINT NOT NULL COMMENT '创建人',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    `update_by` BIGINT DEFAULT NULL COMMENT '更新人',
    `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
    `revision` INT DEFAULT NULL COMMENT '乐观锁',
    PRIMARY KEY (`id`),
    INDEX `idx_provider` (`provider`),
    INDEX `idx_enable_status` (`enable_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI 内容审核规则表';
