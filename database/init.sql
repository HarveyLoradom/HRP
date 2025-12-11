-- HRP系统数据库初始化脚本

CREATE DATABASE IF NOT EXISTS `hrp_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `hrp_db`;

-- ==================== 系统基础模块 ====================

-- 职工表
CREATE TABLE IF NOT EXISTS `sys_emp` (
  `emp_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '职工ID',
  `emp_code` VARCHAR(20) NOT NULL UNIQUE COMMENT '职工编码（工号）',
  `emp_name` VARCHAR(50) NOT NULL COMMENT '职工姓名',
  `emp_sex` BIGINT NOT NULL COMMENT '性别：1-男，2-女',
  `id_card` VARCHAR(18) UNIQUE COMMENT '身份证号',
  `emp_birthday` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '出生日期',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `emp_email` VARCHAR(100) COMMENT '邮箱',
  `emp_phone` VARCHAR(20) COMMENT '手机号',
  `dept_id` BIGINT COMMENT '部门ID',
  `dept_code` VARCHAR(100) COMMENT '部门编码',
  `create_user` VARCHAR(20) COMMENT '创建人',
  `is_stop` BIGINT DEFAULT 0 COMMENT '是否停用：0-否，1-是',
  `emp_type_id` BIGINT COMMENT '职工类型ID',
  KEY `idx_emp_code` (`emp_code`),
  KEY `idx_dept_id` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职工表';

-- 用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` VARCHAR(36) PRIMARY KEY COMMENT '主键ID（UUID）',
  `account` VARCHAR(20) NOT NULL UNIQUE COMMENT '账号（工号）',
  `name` VARCHAR(20) NOT NULL COMMENT '用户名',
  `type` BIGINT DEFAULT 1 COMMENT '用户类型',
  `phone` VARCHAR(20) COMMENT '手机号',
  `password` VARCHAR(255) COMMENT '密码（BCrypt加密）',
  `create_user` VARCHAR(20) COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_stop` BIGINT DEFAULT 0 COMMENT '是否停用：0-否，1-是',
  KEY `idx_account` (`account`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 部门表
CREATE TABLE IF NOT EXISTS `sys_dept` (
  `dept_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '部门ID',
  `dept_code` VARCHAR(20) NOT NULL UNIQUE COMMENT '部门编码',
  `dept_name` VARCHAR(50) NOT NULL COMMENT '部门名称',
  `super_dept_code` VARCHAR(20) COMMENT '上级部门编码',
  `dept_level` BIGINT COMMENT '部门级别',
  `dept_phone` VARCHAR(20) COMMENT '部门电话',
  `dept_manager_id` BIGINT COMMENT '部门负责人ID',
  `is_stop` BIGINT DEFAULT 0 COMMENT '是否停用：0-否，1-是',
  `create_user` VARCHAR(50) COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY `idx_dept_code` (`dept_code`),
  KEY `idx_super_dept_code` (`super_dept_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 字典表
CREATE TABLE IF NOT EXISTS `sys_code` (
  `id` VARCHAR(100) PRIMARY KEY COMMENT '主键ID',
  `code_value` VARCHAR(10) COMMENT '字典值',
  `code_name` VARCHAR(255) COMMENT '字典名称',
  `code_type` VARCHAR(50) COMMENT '字典类型',
  `code_type_name` VARCHAR(50) COMMENT '字典类型名称',
  `is_stop` BIGINT DEFAULT 0 COMMENT '是否停用：0-否，1-是',
  `create_user` VARCHAR(50) COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY `idx_code_type` (`code_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典表';

-- 菜单表
CREATE TABLE IF NOT EXISTS `sys_menu` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `menu_code` VARCHAR(50) NOT NULL COMMENT '菜单编码',
  `menu_name` VARCHAR(100) NOT NULL COMMENT '菜单名称',
  `parent_id` BIGINT(20) DEFAULT 0 COMMENT '父菜单ID，0表示顶级菜单',
  `menu_type` TINYINT(1) DEFAULT 1 COMMENT '菜单类型：1-目录，2-菜单，3-按钮',
  `path` VARCHAR(200) DEFAULT NULL COMMENT '路由路径',
  `component` VARCHAR(200) DEFAULT NULL COMMENT '组件路径',
  `icon` VARCHAR(100) DEFAULT NULL COMMENT '图标',
  `sort` INT(11) DEFAULT 0 COMMENT '排序',
  `status` TINYINT(1) DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_menu_code` (`menu_code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- 用户菜单关联表（用于权限控制）
CREATE TABLE IF NOT EXISTS `sys_user_menu` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` VARCHAR(36) NOT NULL COMMENT '用户ID（UUID）',
  `menu_id` BIGINT(20) NOT NULL COMMENT '菜单ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_menu` (`user_id`, `menu_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_menu_id` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户菜单关联表';

-- 角色表
CREATE TABLE IF NOT EXISTS `sys_role` (
  `role_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
  `role_code` VARCHAR(20) NOT NULL UNIQUE COMMENT '角色编码',
  `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
  `role_desc` VARCHAR(200) COMMENT '角色描述',
  `is_stop` BIGINT DEFAULT 0 COMMENT '是否停用：0-否，1-是',
  `create_user` VARCHAR(50) COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY `idx_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 角色菜单关联表
CREATE TABLE IF NOT EXISTS `sys_role_menu` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `menu_id` BIGINT NOT NULL COMMENT '菜单ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_menu_id` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS `sys_user_role` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `user_id` VARCHAR(36) NOT NULL COMMENT '用户ID（UUID）',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 登录日志表
CREATE TABLE IF NOT EXISTS `sys_login_log` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `account` VARCHAR(50) DEFAULT NULL COMMENT '账号',
  `ip` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
  `status` TINYINT(1) DEFAULT 0 COMMENT '登录状态：0-失败，1-成功',
  `message` VARCHAR(500) DEFAULT NULL COMMENT '登录信息',
  `login_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  PRIMARY KEY (`id`),
  KEY `idx_account` (`account`),
  KEY `idx_login_time` (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

-- 用户登录失败记录表（用于登录失败次数和锁定）
CREATE TABLE IF NOT EXISTS `sys_user_login` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` VARCHAR(36) NOT NULL COMMENT '用户ID（UUID）',
  `account` VARCHAR(20) NOT NULL COMMENT '账号',
  `locked` TINYINT(1) DEFAULT 0 COMMENT '是否锁定：0-否，1-是',
  `lock_time` DATETIME DEFAULT NULL COMMENT '锁定时间',
  `login_fail_count` INT(11) DEFAULT 0 COMMENT '登录失败次数',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_account` (`account`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户登录信息表';

-- ==================== 系统附件模块 ====================

-- 附件表
CREATE TABLE IF NOT EXISTS `sys_attachment` (
  `attachment_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '附件ID',
  `business_type` VARCHAR(50) NOT NULL COMMENT '业务类型：PAYOUT-报账，CONTRACT-合同，BUDGET_EXECUTION-预算执行，BUDGET_ADJUSTMENT-预算调整',
  `business_id` BIGINT NOT NULL COMMENT '业务ID',
  `file_name` VARCHAR(200) NOT NULL COMMENT '文件名称',
  `file_path` VARCHAR(500) NOT NULL COMMENT '文件路径',
  `file_size` BIGINT COMMENT '文件大小（字节）',
  `file_type` VARCHAR(50) COMMENT '文件类型（扩展名）',
  `upload_user` VARCHAR(50) COMMENT '上传人',
  `upload_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `remark` VARCHAR(500) COMMENT '备注',
  KEY `idx_business` (`business_type`, `business_id`),
  KEY `idx_upload_user` (`upload_user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='附件表';

-- ==================== 流程管理模块 ====================

-- 流程定义表
CREATE TABLE IF NOT EXISTS `wf_process_definition` (
  `definition_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '流程定义ID',
  `definition_key` VARCHAR(50) NOT NULL UNIQUE COMMENT '流程定义KEY（如：PAYOUT_APPROVAL、CONTRACT_APPROVAL）',
  `definition_name` VARCHAR(100) NOT NULL COMMENT '流程定义名称',
  `definition_type` VARCHAR(20) NOT NULL COMMENT '流程类型：PAYOUT-报账审批，CONTRACT-合同审批',
  `process_xml` TEXT COMMENT '流程XML定义（BPMN格式）',
  `process_json` TEXT COMMENT '流程JSON定义（用于前端绘制）',
  `version` INT DEFAULT 1 COMMENT '版本号',
  `is_active` BIGINT DEFAULT 1 COMMENT '是否启用：0-否，1-是',
  `description` VARCHAR(500) COMMENT '描述',
  `create_user` VARCHAR(50) COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY `idx_definition_key` (`definition_key`),
  KEY `idx_definition_type` (`definition_type`),
  KEY `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程定义表';

-- 流程实例表
CREATE TABLE IF NOT EXISTS `wf_process_instance` (
  `instance_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '流程实例ID',
  `process_definition_id` BIGINT NOT NULL COMMENT '流程定义ID',
  `business_key` VARCHAR(50) NOT NULL COMMENT '业务主键（单号或合同号）',
  `business_type` VARCHAR(20) NOT NULL COMMENT '业务类型：PAYOUT-报账，CONTRACT-合同',
  `business_id` BIGINT COMMENT '业务ID（payout_id或pact_id）',
  `process_status` VARCHAR(20) DEFAULT 'RUNNING' COMMENT '流程状态：RUNNING-运行中，COMPLETED-已完成，TERMINATED-已终止，SUSPENDED-已挂起',
  `start_user_id` VARCHAR(36) COMMENT '启动人ID',
  `start_user_name` VARCHAR(50) COMMENT '启动人姓名',
  `start_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '启动时间',
  `end_time` DATETIME COMMENT '结束时间',
  `duration` BIGINT COMMENT '持续时间（毫秒）',
  `remark` VARCHAR(500) COMMENT '备注',
  KEY `idx_process_definition_id` (`process_definition_id`),
  KEY `idx_business_key` (`business_key`),
  KEY `idx_business_type` (`business_type`),
  KEY `idx_process_status` (`process_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程实例表';

-- 流程任务表
CREATE TABLE IF NOT EXISTS `wf_process_task` (
  `task_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务ID',
  `process_instance_id` BIGINT NOT NULL COMMENT '流程实例ID',
  `task_key` VARCHAR(50) COMMENT '任务节点KEY',
  `task_name` VARCHAR(100) COMMENT '任务名称',
  `assignee_user_id` VARCHAR(36) COMMENT '办理人ID',
  `assignee_user_name` VARCHAR(50) COMMENT '办理人姓名',
  `assignee_emp_code` VARCHAR(20) COMMENT '办理人工号',
  `candidate_users` VARCHAR(500) COMMENT '候选办理人（JSON格式）',
  `task_status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '任务状态：PENDING-待办理，COMPLETED-已完成，TRANSFERRED-已转办',
  `task_type` VARCHAR(20) COMMENT '任务类型：APPROVE-审批，REVIEW-审核',
  `priority` INT DEFAULT 0 COMMENT '优先级',
  `due_date` DATETIME COMMENT '到期时间',
  `claim_time` DATETIME COMMENT '签收时间',
  `complete_time` DATETIME COMMENT '完成时间',
  `comment` VARCHAR(500) COMMENT '审批意见',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY `idx_process_instance_id` (`process_instance_id`),
  KEY `idx_assignee_user_id` (`assignee_user_id`),
  KEY `idx_task_status` (`task_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程任务表';

-- 流程变量表
CREATE TABLE IF NOT EXISTS `wf_process_variable` (
  `variable_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '变量ID',
  `process_instance_id` BIGINT NOT NULL COMMENT '流程实例ID',
  `variable_key` VARCHAR(50) NOT NULL COMMENT '变量KEY',
  `variable_value` TEXT COMMENT '变量值',
  `variable_type` VARCHAR(20) COMMENT '变量类型：STRING-字符串，INTEGER-整数，DOUBLE-浮点数，BOOLEAN-布尔值，DATE-日期',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY `uk_instance_key` (`process_instance_id`, `variable_key`),
  KEY `idx_process_instance_id` (`process_instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程变量表';

-- ==================== 智能报账模块 ====================

-- 报账表（申请单和报账单共用）
CREATE TABLE IF NOT EXISTS `ctrl_payout` (
  `payout_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '单号ID',
  `payout_billcode` VARCHAR(50) NOT NULL UNIQUE COMMENT '单号',
  `bill_type` VARCHAR(20) DEFAULT 'APPLY' COMMENT '单据类型：APPLY-申请单，PAYOUT-报账单',
  `emp_id` BIGINT NOT NULL COMMENT '申请人ID',
  `emp_code` VARCHAR(20) COMMENT '申请人工号',
  `emp_name` VARCHAR(50) COMMENT '申请人姓名',
  `dept_id` BIGINT COMMENT '部门ID',
  `payout_type_id` VARCHAR(20) COMMENT '申请类型',
  `apply_amount` DECIMAL(18,2) DEFAULT 0.00 COMMENT '申请金额',
  `apply_reason` VARCHAR(500) COMMENT '申请事由',
  `apply_date` DATETIME COMMENT '申请日期',
  `status` VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态：DRAFT-草稿，PENDING-待审批，APPROVED-已审批，REJECTED-已拒绝，WITHDRAWN-已撤回',
  `process_definition_id` BIGINT COMMENT '流程定义ID',
  `process_instance_id` BIGINT COMMENT '流程实例ID',
  `budget_id` BIGINT COMMENT '预算ID',
  `budget_item_id` BIGINT COMMENT '预算项目ID',
  `attachment` VARCHAR(500) COMMENT '附件路径（多个用逗号分隔）',
  `remark` VARCHAR(500) COMMENT '备注',
  `create_user` VARCHAR(50) COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY `idx_apply_no` (`payout_billcode`),
  KEY `idx_emp_id` (`emp_id`),
  KEY `idx_status` (`status`),
  KEY `idx_bill_type` (`bill_type`),
  KEY `idx_process_instance_id` (`process_instance_id`),
  KEY `idx_budget_id` (`budget_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报账表';

-- 报账明细表（报账单使用）
CREATE TABLE IF NOT EXISTS `ctrl_payout_detail` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '明细ID',
  `payout_id` BIGINT NOT NULL COMMENT '报账单ID',
  `item_name` VARCHAR(100) COMMENT '项目名称',
  `item_type` VARCHAR(20) COMMENT '项目类型',
  `amount` DECIMAL(18,2) DEFAULT 0.00 COMMENT '金额',
  `remark` VARCHAR(200) COMMENT '备注',
  KEY `idx_payout_id` (`payout_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报账明细表';

-- 发票表（报账单使用）
CREATE TABLE IF NOT EXISTS `ctrl_payout_invoice` (
  `invoice_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '发票ID',
  `payout_id` BIGINT NOT NULL COMMENT '报账单ID',
  `invoice_code` VARCHAR(50) COMMENT '发票代码',
  `invoice_number` VARCHAR(50) COMMENT '发票号码',
  `invoice_date` DATETIME COMMENT '发票日期',
  `invoice_amount` DECIMAL(18,2) DEFAULT 0.00 COMMENT '发票金额',
  `invoice_type` VARCHAR(20) COMMENT '发票类型：VAT-增值税发票，COMMON-普通发票',
  `tax_amount` DECIMAL(18,2) DEFAULT 0.00 COMMENT '税额',
  `attachment_id` BIGINT COMMENT '发票附件ID（关联sys_attachment表）',
  `remark` VARCHAR(200) COMMENT '备注',
  KEY `idx_payout_id` (`payout_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发票表';

-- 支付清单表（报账单使用）
CREATE TABLE IF NOT EXISTS `ctrl_payout_payment` (
  `payment_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '支付清单ID',
  `payout_id` BIGINT NOT NULL COMMENT '报账单ID',
  `payment_amount` DECIMAL(18,2) DEFAULT 0.00 COMMENT '支付金额',
  `payment_object` VARCHAR(100) COMMENT '支付对象（收款人）',
  `payment_method` VARCHAR(20) COMMENT '支付方式：BANK-银行转账，CASH-现金，CHECK-支票，OTHER-其他',
  `bank_name` VARCHAR(100) COMMENT '银行名称',
  `bank_account` VARCHAR(50) COMMENT '银行账号',
  `account_name` VARCHAR(100) COMMENT '账户名称',
  `payment_date` DATETIME COMMENT '支付日期',
  `remark` VARCHAR(200) COMMENT '备注',
  KEY `idx_payout_id` (`payout_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付清单表';

-- 审批记录表
CREATE TABLE IF NOT EXISTS `ctrl_payout_approval` (
  `approval_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '审批记录ID',
  `payout_id` BIGINT NOT NULL COMMENT '报账单ID',
  `approver_id` BIGINT COMMENT '审批人ID',
  `approver_code` VARCHAR(20) COMMENT '审批人工号',
  `approver_name` VARCHAR(50) COMMENT '审批人姓名',
  `approval_type` VARCHAR(20) COMMENT '审批类型：APPROVE-通过，REJECT-驳回',
  `approval_opinion` VARCHAR(500) COMMENT '审批意见',
  `approval_time` DATETIME COMMENT '审批时间',
  `task_id` BIGINT COMMENT '流程任务ID',
  `task_name` VARCHAR(100) COMMENT '任务名称',
  `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
  KEY `idx_payout_id` (`payout_id`),
  KEY `idx_approver_id` (`approver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批记录表';

-- ==================== 合同管理模块 ====================

-- 合同表
CREATE TABLE IF NOT EXISTS `pact_main` (
  `pact_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '合同ID',
  `contract_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '合同编号',
  `contract_name` VARCHAR(200) NOT NULL COMMENT '合同名称',
  `contract_type` VARCHAR(20) COMMENT '合同类型',
  `party_a` VARCHAR(100) COMMENT '甲方',
  `party_b` VARCHAR(100) COMMENT '乙方',
  `contract_amount` DECIMAL(18,2) DEFAULT 0.00 COMMENT '合同金额',
  `sign_date` DATETIME COMMENT '签订日期',
  `start_date` DATETIME COMMENT '开始日期',
  `end_date` DATETIME COMMENT '结束日期',
  `status` VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态：DRAFT-草稿，PENDING-待审批，APPROVED-已审批，EXECUTING-执行中，ARCHIVED-已归档，REJECTED-已拒绝',
  `dept_id` BIGINT COMMENT '负责部门ID',
  `manager_id` BIGINT COMMENT '负责人ID',
  `process_definition_id` BIGINT COMMENT '流程定义ID',
  `process_instance_id` BIGINT COMMENT '流程实例ID',
  `budget_id` BIGINT COMMENT '预算ID',
  `budget_item_id` BIGINT COMMENT '预算项目ID',
  `remark` VARCHAR(500) COMMENT '备注',
  `create_user` VARCHAR(50) COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY `idx_contract_no` (`contract_no`),
  KEY `idx_status` (`status`),
  KEY `idx_process_instance_id` (`process_instance_id`),
  KEY `idx_budget_id` (`budget_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合同表';

-- ==================== 预算管理模块 ====================

-- 预算主体表
CREATE TABLE IF NOT EXISTS `budget_subject` (
  `subject_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '预算主体ID',
  `subject_code` VARCHAR(50) NOT NULL UNIQUE COMMENT '预算主体编码',
  `subject_name` VARCHAR(100) NOT NULL COMMENT '预算主体名称',
  `subject_type` VARCHAR(20) COMMENT '主体类型：CLINIC-临床科室，ADMIN-行政部门，TECH-医技科室',
  `parent_subject_id` BIGINT COMMENT '上级预算主体ID',
  `parent_subject_code` VARCHAR(50) COMMENT '上级预算主体编码',
  `dept_id` BIGINT COMMENT '责任科室ID',
  `dept_code` VARCHAR(20) COMMENT '责任科室编码',
  `manager_id` BIGINT COMMENT '负责人ID',
  `manager_code` VARCHAR(20) COMMENT '负责人工号',
  `manager_name` VARCHAR(50) COMMENT '负责人姓名',
  `subject_level` INT DEFAULT 1 COMMENT '层级（1-一级，2-二级，3-三级）',
  `is_stop` BIGINT DEFAULT 0 COMMENT '是否停用：0-否，1-是',
  `create_user` VARCHAR(50) COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY `idx_subject_code` (`subject_code`),
  KEY `idx_parent_subject_id` (`parent_subject_id`),
  KEY `idx_dept_id` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预算主体表';

-- 预算项目分类表
CREATE TABLE IF NOT EXISTS `budget_category` (
  `category_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
  `category_code` VARCHAR(50) NOT NULL UNIQUE COMMENT '分类编码',
  `category_name` VARCHAR(100) NOT NULL COMMENT '分类名称',
  `category_type` VARCHAR(20) NOT NULL COMMENT '分类类型：INCOME-收入预算，EXPENSE-支出预算，SPECIAL-专项预算',
  `parent_category_id` BIGINT COMMENT '上级分类ID',
  `parent_category_code` VARCHAR(50) COMMENT '上级分类编码',
  `category_level` INT DEFAULT 1 COMMENT '层级',
  `is_stop` BIGINT DEFAULT 0 COMMENT '是否停用：0-否，1-是',
  `create_user` VARCHAR(50) COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY `idx_category_code` (`category_code`),
  KEY `idx_category_type` (`category_type`),
  KEY `idx_parent_category_id` (`parent_category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预算项目分类表';

-- 预算项目表
CREATE TABLE IF NOT EXISTS `budget_item` (
  `item_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '预算项目ID',
  `item_code` VARCHAR(50) NOT NULL UNIQUE COMMENT '项目编码',
  `item_name` VARCHAR(100) NOT NULL COMMENT '项目名称',
  `category_id` BIGINT NOT NULL COMMENT '分类ID',
  `category_code` VARCHAR(50) COMMENT '分类编码',
  `account_subject` VARCHAR(50) COMMENT '关联会计科目',
  `is_centralized` BIGINT DEFAULT 0 COMMENT '是否归口管理：0-否，1-是',
  `allow_adjust` BIGINT DEFAULT 1 COMMENT '是否允许调整：0-否，1-是',
  `item_desc` VARCHAR(500) COMMENT '项目描述',
  `is_stop` BIGINT DEFAULT 0 COMMENT '是否停用：0-否，1-是',
  `create_user` VARCHAR(50) COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY `idx_item_code` (`item_code`),
  KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预算项目表';

-- 预算表（年度预算，关联预算主体和预算项目）
CREATE TABLE IF NOT EXISTS `budget` (
  `budget_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '预算ID',
  `budget_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '预算编号',
  `budget_name` VARCHAR(200) NOT NULL COMMENT '预算名称',
  `budget_year` VARCHAR(4) NOT NULL COMMENT '预算年度',
  `budget_period` VARCHAR(20) DEFAULT 'YEAR' COMMENT '预算周期：YEAR-年度，QUARTER-季度，MONTH-月度',
  `subject_id` BIGINT NOT NULL COMMENT '预算主体ID',
  `subject_code` VARCHAR(50) COMMENT '预算主体编码',
  `item_id` BIGINT NOT NULL COMMENT '预算项目ID',
  `item_code` VARCHAR(50) COMMENT '预算项目编码',
  `budget_amount` DECIMAL(18,2) DEFAULT 0.00 COMMENT '预算金额',
  `occupied_amount` DECIMAL(18,2) DEFAULT 0.00 COMMENT '占用金额（已申请但未执行）',
  `executed_amount` DECIMAL(18,2) DEFAULT 0.00 COMMENT '已执行金额',
  `remaining_amount` DECIMAL(18,2) DEFAULT 0.00 COMMENT '剩余金额',
  `execution_rate` DECIMAL(5,2) DEFAULT 0.00 COMMENT '执行率（%）',
  `status` VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态：DRAFT-草稿，APPROVED-已审批，EXECUTING-执行中',
  `process_definition_id` BIGINT COMMENT '流程定义ID',
  `process_instance_id` BIGINT COMMENT '流程实例ID',
  `create_user` VARCHAR(50) COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY `idx_budget_no` (`budget_no`),
  KEY `idx_budget_year` (`budget_year`),
  KEY `idx_subject_id` (`subject_id`),
  KEY `idx_item_id` (`item_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预算表';

-- 预算执行单表
CREATE TABLE IF NOT EXISTS `budget_execution` (
  `execution_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '执行单ID',
  `execution_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '执行单号',
  `budget_id` BIGINT NOT NULL COMMENT '预算ID',
  `budget_no` VARCHAR(50) COMMENT '预算编号',
  `subject_id` BIGINT COMMENT '预算主体ID',
  `item_id` BIGINT COMMENT '预算项目ID',
  `execution_type` VARCHAR(20) DEFAULT 'AUTO' COMMENT '执行类型：AUTO-自动同步，MANUAL-手工录入',
  `execution_amount` DECIMAL(18,2) DEFAULT 0.00 COMMENT '执行金额',
  `execution_date` DATETIME COMMENT '执行日期',
  `business_type` VARCHAR(20) COMMENT '业务类型：PAYOUT-报账，CONTRACT-合同，PROCUREMENT-采购',
  `business_id` BIGINT COMMENT '业务ID',
  `business_no` VARCHAR(50) COMMENT '业务单号',
  `execution_reason` VARCHAR(500) COMMENT '执行事由',
  `attachment` VARCHAR(500) COMMENT '附件路径',
  `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING-待审核，APPROVED-已审核，REJECTED-已拒绝，CANCELLED-已作废',
  `audit_user` VARCHAR(50) COMMENT '审核人',
  `audit_time` DATETIME COMMENT '审核时间',
  `audit_opinion` VARCHAR(500) COMMENT '审核意见',
  `create_user` VARCHAR(50) COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY `idx_execution_no` (`execution_no`),
  KEY `idx_budget_id` (`budget_id`),
  KEY `idx_business_id` (`business_id`),
  KEY `idx_execution_date` (`execution_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预算执行单表';

-- 预算调整表
CREATE TABLE IF NOT EXISTS `budget_adjustment` (
  `adjustment_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '调整ID',
  `adjustment_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '调整单号',
  `budget_id` BIGINT NOT NULL COMMENT '预算ID',
  `budget_no` VARCHAR(50) COMMENT '预算编号',
  `subject_id` BIGINT COMMENT '预算主体ID',
  `item_id` BIGINT COMMENT '预算项目ID',
  `original_amount` DECIMAL(18,2) DEFAULT 0.00 COMMENT '原预算金额',
  `adjustment_amount` DECIMAL(18,2) DEFAULT 0.00 COMMENT '调整金额（正数为增加，负数为减少）',
  `adjusted_amount` DECIMAL(18,2) DEFAULT 0.00 COMMENT '调整后金额',
  `adjustment_reason` VARCHAR(500) COMMENT '调整原因',
  `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING-待审批，APPROVED-已审批，REJECTED-已拒绝',
  `process_definition_id` BIGINT COMMENT '流程定义ID',
  `process_instance_id` BIGINT COMMENT '流程实例ID',
  `applicant_id` BIGINT COMMENT '申请人ID',
  `applicant_name` VARCHAR(50) COMMENT '申请人姓名',
  `approver_id` BIGINT COMMENT '审批人ID',
  `approver_name` VARCHAR(50) COMMENT '审批人姓名',
  `approve_time` DATETIME COMMENT '审批时间',
  `approve_opinion` VARCHAR(500) COMMENT '审批意见',
  `create_user` VARCHAR(50) COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY `idx_adjustment_no` (`adjustment_no`),
  KEY `idx_budget_id` (`budget_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预算调整表';

-- 预算流程配置表
CREATE TABLE IF NOT EXISTS `budget_process_config` (
  `config_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
  `config_type` VARCHAR(20) NOT NULL COMMENT '配置类型：BUDGET-预算编制，ADJUSTMENT-预算调整',
  `process_definition_id` BIGINT COMMENT '流程定义ID',
  `config_json` TEXT COMMENT '配置JSON（审批节点、审批人、时效等）',
  `is_active` BIGINT DEFAULT 1 COMMENT '是否启用：0-否，1-是',
  `create_user` VARCHAR(50) COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY `idx_config_type` (`config_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预算流程配置表';

-- 预算参数设置表
CREATE TABLE IF NOT EXISTS `budget_param` (
  `param_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '参数ID',
  `param_code` VARCHAR(50) NOT NULL UNIQUE COMMENT '参数编码',
  `param_name` VARCHAR(100) NOT NULL COMMENT '参数名称',
  `param_value` VARCHAR(200) COMMENT '参数值',
  `param_type` VARCHAR(20) COMMENT '参数类型：PERIOD-预算周期，WARNING-预警阈值，INTERCEPT-拦截规则',
  `param_desc` VARCHAR(500) COMMENT '参数描述',
  `is_stop` BIGINT DEFAULT 0 COMMENT '是否停用：0-否，1-是',
  `create_user` VARCHAR(50) COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY `idx_param_code` (`param_code`),
  KEY `idx_param_type` (`param_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预算参数设置表';

-- ==================== 固定资产模块 ====================

-- 固定资产表
CREATE TABLE IF NOT EXISTS `fixed_asset` (
  `asset_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '资产ID',
  `asset_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '资产编号',
  `asset_name` VARCHAR(100) NOT NULL COMMENT '资产名称',
  `asset_type` VARCHAR(20) COMMENT '资产类型',
  `asset_category` VARCHAR(20) COMMENT '资产类别',
  `purchase_date` DATETIME COMMENT '采购日期',
  `purchase_amount` DECIMAL(18,2) DEFAULT 0.00 COMMENT '采购金额',
  `current_value` DECIMAL(18,2) DEFAULT 0.00 COMMENT '当前价值',
  `dept_id` BIGINT COMMENT '使用部门ID',
  `manager_id` BIGINT COMMENT '负责人ID',
  `location` VARCHAR(100) COMMENT '存放位置',
  `status` VARCHAR(20) DEFAULT 'NORMAL' COMMENT '状态：NORMAL-正常，MAINTENANCE-维修，SCRAPPED-报废',
  `remark` VARCHAR(500) COMMENT '备注',
  `create_user` VARCHAR(50) COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY `idx_asset_no` (`asset_no`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='固定资产表';

-- 采购需求表
CREATE TABLE IF NOT EXISTS `procurement_requirement` (
  `requirement_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '需求ID',
  `requirement_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '需求单号',
  `requirement_name` VARCHAR(200) NOT NULL COMMENT '需求名称',
  `requirement_type` VARCHAR(20) COMMENT '需求类型',
  `dept_id` BIGINT COMMENT '申请部门ID',
  `dept_code` VARCHAR(50) COMMENT '申请部门编码',
  `dept_name` VARCHAR(100) COMMENT '申请部门名称',
  `applicant_id` BIGINT COMMENT '申请人ID',
  `applicant_code` VARCHAR(50) COMMENT '申请人编码',
  `applicant_name` VARCHAR(100) COMMENT '申请人姓名',
  `estimated_amount` DECIMAL(18,2) DEFAULT 0.00 COMMENT '预估金额',
  `requirement_desc` VARCHAR(1000) COMMENT '需求描述',
  `status` VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态：DRAFT-草稿，PENDING-待审批，APPROVED-已审批，REJECTED-已拒绝',
  `process_definition_id` BIGINT COMMENT '流程定义ID',
  `process_instance_id` BIGINT COMMENT '流程实例ID',
  `create_user` VARCHAR(50) COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY `idx_requirement_no` (`requirement_no`),
  KEY `idx_status` (`status`),
  KEY `idx_applicant_id` (`applicant_id`),
  KEY `idx_dept_id` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购需求表';

-- 采购需求明细表
CREATE TABLE IF NOT EXISTS `procurement_requirement_detail` (
  `detail_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '明细ID',
  `requirement_id` BIGINT NOT NULL COMMENT '需求ID',
  `asset_name` VARCHAR(200) NOT NULL COMMENT '资产名称',
  `asset_type` VARCHAR(50) COMMENT '资产类型',
  `asset_spec` VARCHAR(200) COMMENT '规格型号',
  `quantity` INT DEFAULT 1 COMMENT '数量',
  `unit_price` DECIMAL(18,2) DEFAULT 0.00 COMMENT '单价',
  `total_amount` DECIMAL(18,2) DEFAULT 0.00 COMMENT '总金额',
  `purpose` VARCHAR(500) COMMENT '用途说明',
  `remark` VARCHAR(500) COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  KEY `idx_requirement_id` (`requirement_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购需求明细表';

-- 采购需求审批记录表
CREATE TABLE IF NOT EXISTS `procurement_requirement_approval` (
  `approval_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '审批记录ID',
  `requirement_id` BIGINT NOT NULL COMMENT '需求ID',
  `approver_id` VARCHAR(50) COMMENT '审批人ID',
  `approver_code` VARCHAR(50) COMMENT '审批人编码',
  `approver_name` VARCHAR(100) COMMENT '审批人姓名',
  `approval_type` VARCHAR(20) COMMENT '审批类型：APPROVE-同意，REJECT-拒绝',
  `approval_opinion` VARCHAR(500) COMMENT '审批意见',
  `approval_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '审批时间',
  `task_id` BIGINT COMMENT '流程任务ID',
  KEY `idx_requirement_id` (`requirement_id`),
  KEY `idx_approver_id` (`approver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购需求审批记录表';

-- 资产审批表
CREATE TABLE IF NOT EXISTS `asset_approval` (
  `approval_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '审批ID',
  `approval_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '审批单号',
  `approval_type` VARCHAR(20) NOT NULL COMMENT '审批类型：TRANSFER-资产调拨，DISPOSAL-资产处置，INVENTORY_DIFF-盘点差异，CHANGE-资产变动',
  `approval_title` VARCHAR(200) NOT NULL COMMENT '审批标题',
  `dept_id` BIGINT COMMENT '申请部门ID',
  `dept_code` VARCHAR(50) COMMENT '申请部门编码',
  `dept_name` VARCHAR(100) COMMENT '申请部门名称',
  `applicant_id` BIGINT COMMENT '申请人ID',
  `applicant_code` VARCHAR(50) COMMENT '申请人编码',
  `applicant_name` VARCHAR(100) COMMENT '申请人姓名',
  `apply_reason` VARCHAR(1000) COMMENT '申请原因',
  `status` VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态：DRAFT-草稿，PENDING-待审批，APPROVED-已审批，REJECTED-已拒绝',
  `process_definition_id` BIGINT COMMENT '流程定义ID',
  `process_instance_id` BIGINT COMMENT '流程实例ID',
  `create_user` VARCHAR(50) COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY `idx_approval_no` (`approval_no`),
  KEY `idx_approval_type` (`approval_type`),
  KEY `idx_status` (`status`),
  KEY `idx_applicant_id` (`applicant_id`),
  KEY `idx_dept_id` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产审批表';

-- 资产审批明细表
CREATE TABLE IF NOT EXISTS `asset_approval_detail` (
  `detail_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '明细ID',
  `approval_id` BIGINT NOT NULL COMMENT '审批ID',
  `asset_id` BIGINT COMMENT '资产ID',
  `asset_no` VARCHAR(50) COMMENT '资产编号',
  `asset_name` VARCHAR(200) COMMENT '资产名称',
  `asset_type` VARCHAR(50) COMMENT '资产类型',
  `original_dept_id` BIGINT COMMENT '原部门ID',
  `original_dept_name` VARCHAR(100) COMMENT '原部门名称',
  `target_dept_id` BIGINT COMMENT '目标部门ID',
  `target_dept_name` VARCHAR(100) COMMENT '目标部门名称',
  `original_location` VARCHAR(200) COMMENT '原存放位置',
  `target_location` VARCHAR(200) COMMENT '目标存放位置',
  `original_value` DECIMAL(18,2) COMMENT '原价值',
  `new_value` DECIMAL(18,2) COMMENT '新价值',
  `disposal_method` VARCHAR(50) COMMENT '处置方式',
  `disposal_amount` DECIMAL(18,2) COMMENT '处置金额',
  `inventory_diff` VARCHAR(500) COMMENT '盘点差异说明',
  `change_content` VARCHAR(1000) COMMENT '变动内容',
  `remark` VARCHAR(500) COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  KEY `idx_approval_id` (`approval_id`),
  KEY `idx_asset_id` (`asset_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产审批明细表';

-- 资产审批记录表
CREATE TABLE IF NOT EXISTS `asset_approval_record` (
  `record_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
  `approval_id` BIGINT NOT NULL COMMENT '审批ID',
  `approver_id` VARCHAR(50) COMMENT '审批人ID',
  `approver_code` VARCHAR(50) COMMENT '审批人编码',
  `approver_name` VARCHAR(100) COMMENT '审批人姓名',
  `approval_type` VARCHAR(20) COMMENT '审批类型：APPROVE-同意，REJECT-拒绝',
  `approval_opinion` VARCHAR(500) COMMENT '审批意见',
  `approval_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '审批时间',
  `task_id` BIGINT COMMENT '流程任务ID',
  KEY `idx_approval_id` (`approval_id`),
  KEY `idx_approver_id` (`approver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资产审批记录表';

-- ==================== 全景人力模块 ====================

-- 薪酬表
CREATE TABLE IF NOT EXISTS `salary` (
  `salary_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '薪酬ID',
  `emp_id` BIGINT NOT NULL COMMENT '职工ID',
  `emp_code` VARCHAR(20) COMMENT '工号',
  `emp_name` VARCHAR(50) COMMENT '姓名',
  `salary_month` VARCHAR(7) NOT NULL COMMENT '薪酬月份（YYYY-MM）',
  `base_salary` DECIMAL(18,2) DEFAULT 0.00 COMMENT '基本工资',
  `performance_salary` DECIMAL(18,2) DEFAULT 0.00 COMMENT '绩效工资',
  `allowance` DECIMAL(18,2) DEFAULT 0.00 COMMENT '津贴',
  `bonus` DECIMAL(18,2) DEFAULT 0.00 COMMENT '奖金',
  `deduction` DECIMAL(18,2) DEFAULT 0.00 COMMENT '扣除',
  `total_salary` DECIMAL(18,2) DEFAULT 0.00 COMMENT '应发工资',
  `tax` DECIMAL(18,2) DEFAULT 0.00 COMMENT '个人所得税',
  `actual_salary` DECIMAL(18,2) DEFAULT 0.00 COMMENT '实发工资',
  `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING-待计算，CALCULATED-已计算，PAID-已发放',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY `uk_emp_month` (`emp_id`, `salary_month`),
  KEY `idx_emp_code` (`emp_code`),
  KEY `idx_salary_month` (`salary_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='薪酬表';

-- 薪酬计算规则表
CREATE TABLE IF NOT EXISTS `salary_rule` (
  `rule_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '规则ID',
  `rule_code` VARCHAR(20) NOT NULL UNIQUE COMMENT '规则编码',
  `rule_name` VARCHAR(50) NOT NULL COMMENT '规则名称',
  `rule_type` VARCHAR(20) COMMENT '规则类型',
  `rule_formula` VARCHAR(200) COMMENT '计算公式',
  `is_active` BIGINT DEFAULT 1 COMMENT '是否启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  KEY `idx_rule_code` (`rule_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='薪酬计算规则表';

-- ==================== DIP成本模块 ====================

-- 成本报表表
CREATE TABLE IF NOT EXISTS `cost_report` (
  `report_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '报表ID',
  `report_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '报表编号',
  `report_name` VARCHAR(200) NOT NULL COMMENT '报表名称',
  `report_type` VARCHAR(20) COMMENT '报表类型',
  `report_period` VARCHAR(20) COMMENT '报表期间',
  `dept_id` BIGINT COMMENT '部门ID',
  `total_cost` DECIMAL(18,2) DEFAULT 0.00 COMMENT '总成本',
  `direct_cost` DECIMAL(18,2) DEFAULT 0.00 COMMENT '直接成本',
  `indirect_cost` DECIMAL(18,2) DEFAULT 0.00 COMMENT '间接成本',
  `status` VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态',
  `create_user` VARCHAR(50) COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  KEY `idx_report_no` (`report_no`),
  KEY `idx_report_period` (`report_period`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成本报表表';

-- 成本分析表
CREATE TABLE IF NOT EXISTS `cost_analysis` (
  `analysis_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分析ID',
  `analysis_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '分析编号',
  `analysis_name` VARCHAR(200) NOT NULL COMMENT '分析名称',
  `analysis_type` VARCHAR(20) COMMENT '分析类型',
  `analysis_period` VARCHAR(20) COMMENT '分析期间',
  `dept_id` BIGINT COMMENT '部门ID',
  `analysis_result` TEXT COMMENT '分析结果',
  `create_user` VARCHAR(50) COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  KEY `idx_analysis_no` (`analysis_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成本分析表';

-- 成本核算表
CREATE TABLE IF NOT EXISTS `cost_accounting` (
  `accounting_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '核算ID',
  `accounting_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '核算编号',
  `accounting_name` VARCHAR(200) NOT NULL COMMENT '核算名称',
  `accounting_period` VARCHAR(20) COMMENT '核算期间',
  `dept_id` BIGINT COMMENT '部门ID',
  `cost_item` VARCHAR(50) COMMENT '成本项目',
  `cost_amount` DECIMAL(18,2) DEFAULT 0.00 COMMENT '成本金额',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  KEY `idx_accounting_no` (`accounting_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成本核算表';

-- ==================== 单机效能模块 ====================

-- 数据采集表（科室）
CREATE TABLE IF NOT EXISTS `data_collection_clinic` (
  `collection_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '采集ID',
  `clinic_id` BIGINT NOT NULL COMMENT '科室ID',
  `clinic_code` VARCHAR(20) COMMENT '科室编码',
  `clinic_name` VARCHAR(50) COMMENT '科室名称',
  `collection_date` DATE NOT NULL COMMENT '采集日期',
  `collection_type` VARCHAR(20) COMMENT '采集类型',
  `collection_data` TEXT COMMENT '采集数据（JSON格式）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  KEY `idx_clinic_id` (`clinic_id`),
  KEY `idx_collection_date` (`collection_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科室数据采集表';

-- 数据采集表（设备）
CREATE TABLE IF NOT EXISTS `data_collection_equipment` (
  `collection_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '采集ID',
  `equipment_id` BIGINT NOT NULL COMMENT '设备ID',
  `equipment_code` VARCHAR(50) COMMENT '设备编码',
  `equipment_name` VARCHAR(100) COMMENT '设备名称',
  `collection_date` DATE NOT NULL COMMENT '采集日期',
  `collection_type` VARCHAR(20) COMMENT '采集类型',
  `collection_data` TEXT COMMENT '采集数据（JSON格式）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  KEY `idx_equipment_id` (`equipment_id`),
  KEY `idx_collection_date` (`collection_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备数据采集表';

-- 收入数据表（HIS收入）
CREATE TABLE IF NOT EXISTS `income_his` (
  `income_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '收入ID',
  `income_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '收入单号',
  `income_date` DATE NOT NULL COMMENT '收入日期',
  `dept_id` BIGINT COMMENT '部门ID',
  `clinic_id` BIGINT COMMENT '科室ID',
  `income_type` VARCHAR(20) COMMENT '收入类型',
  `income_amount` DECIMAL(18,2) DEFAULT 0.00 COMMENT '收入金额',
  `his_system_no` VARCHAR(50) COMMENT 'HIS系统单号',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  KEY `idx_income_no` (`income_no`),
  KEY `idx_income_date` (`income_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='HIS收入数据表';

-- 收入数据表（设备收入）
CREATE TABLE IF NOT EXISTS `income_equipment` (
  `income_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '收入ID',
  `income_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '收入单号',
  `income_date` DATE NOT NULL COMMENT '收入日期',
  `equipment_id` BIGINT COMMENT '设备ID',
  `equipment_code` VARCHAR(50) COMMENT '设备编码',
  `income_type` VARCHAR(20) COMMENT '收入类型',
  `income_amount` DECIMAL(18,2) DEFAULT 0.00 COMMENT '收入金额',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  KEY `idx_income_no` (`income_no`),
  KEY `idx_equipment_id` (`equipment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备收入数据表';

-- 成本数据表
CREATE TABLE IF NOT EXISTS `cost_data` (
  `cost_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '成本ID',
  `cost_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '成本单号',
  `cost_date` DATE NOT NULL COMMENT '成本日期',
  `dept_id` BIGINT COMMENT '部门ID',
  `cost_type` VARCHAR(20) COMMENT '成本类型',
  `cost_item` VARCHAR(50) COMMENT '成本项目',
  `cost_amount` DECIMAL(18,2) DEFAULT 0.00 COMMENT '成本金额',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  KEY `idx_cost_no` (`cost_no`),
  KEY `idx_cost_date` (`cost_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成本数据表';

-- 设备分析报告表
CREATE TABLE IF NOT EXISTS `equipment_analysis_report` (
  `report_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '报告ID',
  `report_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '报告编号',
  `report_name` VARCHAR(200) NOT NULL COMMENT '报告名称',
  `equipment_id` BIGINT COMMENT '设备ID',
  `equipment_code` VARCHAR(50) COMMENT '设备编码',
  `report_period` VARCHAR(20) COMMENT '报告期间',
  `report_content` TEXT COMMENT '报告内容',
  `analysis_result` TEXT COMMENT '分析结果',
  `create_user` VARCHAR(50) COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  KEY `idx_report_no` (`report_no`),
  KEY `idx_equipment_id` (`equipment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备分析报告表';

-- 投资收益分析表
CREATE TABLE IF NOT EXISTS `investment_return_analysis` (
  `analysis_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分析ID',
  `analysis_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '分析编号',
  `analysis_name` VARCHAR(200) NOT NULL COMMENT '分析名称',
  `equipment_id` BIGINT COMMENT '设备ID',
  `investment_amount` DECIMAL(18,2) DEFAULT 0.00 COMMENT '投资金额',
  `return_amount` DECIMAL(18,2) DEFAULT 0.00 COMMENT '回报金额',
  `return_rate` DECIMAL(5,2) DEFAULT 0.00 COMMENT '回报率（%）',
  `analysis_period` VARCHAR(20) COMMENT '分析期间',
  `analysis_result` TEXT COMMENT '分析结果',
  `create_user` VARCHAR(50) COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  KEY `idx_analysis_no` (`analysis_no`),
  KEY `idx_equipment_id` (`equipment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投资收益分析表';

-- 设备表
CREATE TABLE IF NOT EXISTS `equipment` (
  `equipment_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '设备ID',
  `equipment_code` VARCHAR(50) NOT NULL UNIQUE COMMENT '设备编码',
  `equipment_name` VARCHAR(100) NOT NULL COMMENT '设备名称',
  `equipment_type` VARCHAR(20) COMMENT '设备类型',
  `purchase_date` DATETIME COMMENT '采购日期',
  `purchase_amount` DECIMAL(18,2) DEFAULT 0.00 COMMENT '采购金额',
  `dept_id` BIGINT COMMENT '使用部门ID',
  `clinic_id` BIGINT COMMENT '使用科室ID',
  `status` VARCHAR(20) DEFAULT 'NORMAL' COMMENT '状态：NORMAL-正常，MAINTENANCE-维修，SCRAPPED-报废',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY `idx_equipment_code` (`equipment_code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备表';
