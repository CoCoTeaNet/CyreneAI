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
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_conversation_id` (`conversation_id`),
    INDEX `idx_created_time` (`created_time`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='AI消息表';
