-- HRP系统数据库初始化脚本

CREATE DATABASE IF NOT EXISTS `hrp_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `hrp_db`;

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




-- 报账表
CREATE TABLE IF NOT EXISTS `ctrl_payout` (
  `payout_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '单号ID',
  `payout_billcode` VARCHAR(50) NOT NULL UNIQUE COMMENT '单号',
  `emp_id` BIGINT NOT NULL COMMENT '申请人ID',
  `emp_code` VARCHAR(20) COMMENT '申请人工号',
  `emp_name` VARCHAR(50) COMMENT '申请人姓名',
  `dept_id` BIGINT COMMENT '部门ID',
  `payout_type_id` VARCHAR(20) COMMENT '申请类型',
  `apply_amount` DECIMAL(18,2) DEFAULT 0.00 COMMENT '申请金额',
  `apply_reason` VARCHAR(500) COMMENT '申请事由',
  `apply_date` DATETIME COMMENT '申请日期',
  `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING-待审批，APPROVED-已审批，REJECTED-已拒绝',
  `remark` VARCHAR(500) COMMENT '备注',
  `create_user` VARCHAR(50) COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY `idx_apply_no` (`payout_billcode`),
  KEY `idx_emp_id` (`emp_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报账表';



-- 报账明细表
CREATE TABLE IF NOT EXISTS `ctrl_payout_detail` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '明细ID',
  `payout_id` BIGINT NOT NULL COMMENT '申请ID',
  `item_name` VARCHAR(100) COMMENT '项目名称',
  `item_type` VARCHAR(20) COMMENT '项目类型',
  `amount` DECIMAL(18,2) DEFAULT 0.00 COMMENT '金额',
  `invoice_no` VARCHAR(50) COMMENT '发票号',
  `invoice_date` DATETIME COMMENT '发票日期',
  `remark` VARCHAR(200) COMMENT '备注',
  KEY `idx_apply_id` (`payout_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报账明细表';



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
  `status` VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态：DRAFT-草稿，PENDING-待审批，APPROVED-已审批，EXECUTING-执行中，ARCHIVED-已归档',
  `dept_id` BIGINT COMMENT '负责部门ID',
  `manager_id` BIGINT COMMENT '负责人ID',
  `remark` VARCHAR(500) COMMENT '备注',
  `create_user` VARCHAR(50) COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY `idx_contract_no` (`contract_no`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合同表';



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
  `applicant_id` BIGINT COMMENT '申请人ID',
  `estimated_amount` DECIMAL(18,2) DEFAULT 0.00 COMMENT '预估金额',
  `requirement_desc` VARCHAR(1000) COMMENT '需求描述',
  `status` VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING-待审批，APPROVED-已审批，REJECTED-已拒绝',
  `create_user` VARCHAR(50) COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY `idx_requirement_no` (`requirement_no`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购需求表';

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
