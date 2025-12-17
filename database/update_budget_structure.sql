-- 预算管理模块结构调整SQL脚本
-- 执行日期：根据实际需要

-- ==================== 1. 修改预算主体表结构 ====================
-- 删除不需要的字段：parent_subject_id, parent_subject_code, dept_id, dept_code, manager_id, manager_code, manager_name, subject_level
-- 新增字段：归口部门(manage_dept_id, manage_dept_code, manage_dept_name), 归口负责人(manage_emp_id, manage_emp_code, manage_emp_name)
-- 主体类型改为从sys_code表获取，需要添加SUBJECT_TYPE代码类型

-- 先添加主体类型到sys_code表（如果不存在）
INSERT INTO `sys_code` (`id`, `code_type`, `code_type_name`, `code_value`, `code_name`, `is_stop`, `create_user`) VALUES
(UUID(), 'SUBJECT_TYPE', '主体类型', 'CLINIC', '临床科室', 0, 'SYSTEM'),
(UUID(), 'SUBJECT_TYPE', '主体类型', 'TECH', '医技科室', 0, 'SYSTEM'),
(UUID(), 'SUBJECT_TYPE', '主体类型', 'ADMIN', '行政部门', 0, 'SYSTEM'),
(UUID(), 'SUBJECT_TYPE', '主体类型', 'FUNC', '职能部门', 0, 'SYSTEM')
ON DUPLICATE KEY UPDATE code_name = VALUES(code_name);

-- 修改预算主体表结构
ALTER TABLE `budget_subject`
  DROP COLUMN IF EXISTS `parent_subject_id`,
  DROP COLUMN IF EXISTS `parent_subject_code`,
  DROP COLUMN IF EXISTS `dept_id`,
  DROP COLUMN IF EXISTS `dept_code`,
  DROP COLUMN IF EXISTS `manager_id`,
  DROP COLUMN IF EXISTS `manager_code`,
  DROP COLUMN IF EXISTS `manager_name`,
  DROP COLUMN IF EXISTS `subject_level`,
  ADD COLUMN `manage_dept_id` BIGINT COMMENT '归口部门ID' AFTER `subject_type`,
  ADD COLUMN `manage_dept_code` VARCHAR(20) COMMENT '归口部门编码' AFTER `manage_dept_id`,
  ADD COLUMN `manage_dept_name` VARCHAR(100) COMMENT '归口部门名称' AFTER `manage_dept_code`,
  ADD COLUMN `manage_emp_id` BIGINT COMMENT '归口负责人ID' AFTER `manage_dept_name`,
  ADD COLUMN `manage_emp_code` VARCHAR(20) COMMENT '归口负责人工号' AFTER `manage_emp_id`,
  ADD COLUMN `manage_emp_name` VARCHAR(50) COMMENT '归口负责人姓名' AFTER `manage_emp_code`,
  ADD KEY `idx_manage_dept_id` (`manage_dept_id`),
  ADD KEY `idx_manage_emp_id` (`manage_emp_id`);

-- 创建预算主体关联科室表（支持多选科室）
CREATE TABLE IF NOT EXISTS `budget_subject_dept` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
  `subject_id` BIGINT NOT NULL COMMENT '预算主体ID',
  `dept_id` BIGINT NOT NULL COMMENT '关联的科室ID',
  `dept_code` VARCHAR(20) COMMENT '关联的科室编码',
  `dept_name` VARCHAR(100) COMMENT '关联的科室名称',
  `create_user` VARCHAR(50) COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  KEY `idx_subject_id` (`subject_id`),
  KEY `idx_dept_id` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预算主体关联科室表（支持一个主体关联多个科室）';

-- ==================== 2. 修改预算分类表结构 ====================
-- 添加年份字段（必填）
ALTER TABLE `budget_category`
  ADD COLUMN `budget_year` VARCHAR(4) NOT NULL DEFAULT '' COMMENT '预算年度' AFTER `category_name`,
  ADD KEY `idx_budget_year` (`budget_year`);

-- ==================== 3. 修改预算项目表结构 ====================
-- 添加年份字段（必填）
ALTER TABLE `budget_item`
  ADD COLUMN `budget_year` VARCHAR(4) NOT NULL DEFAULT '' COMMENT '预算年度' AFTER `item_name`,
  ADD KEY `idx_budget_year` (`budget_year`);

-- 创建预算项目主体分配表（项目可以分配给多个主体）
CREATE TABLE IF NOT EXISTS `budget_item_subject` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
  `item_id` BIGINT NOT NULL COMMENT '预算项目ID',
  `subject_id` BIGINT NOT NULL COMMENT '预算主体ID',
  `subject_code` VARCHAR(50) COMMENT '预算主体编码',
  `subject_name` VARCHAR(100) COMMENT '预算主体名称',
  `create_user` VARCHAR(50) COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  KEY `idx_item_id` (`item_id`),
  KEY `idx_subject_id` (`subject_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预算项目主体分配表';

-- ==================== 3. 创建预算申请表 ====================
CREATE TABLE IF NOT EXISTS `budget_apply` (
  `apply_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '申请ID',
  `apply_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '申请单号',
  `budget_year` VARCHAR(4) NOT NULL COMMENT '预算年度',
  `item_id` BIGINT NOT NULL COMMENT '预算项目ID',
  `item_code` VARCHAR(50) COMMENT '预算项目编码',
  `item_name` VARCHAR(100) COMMENT '预算项目名称',
  `subject_id` BIGINT COMMENT '预算主体ID',
  `subject_code` VARCHAR(50) COMMENT '预算主体编码',
  `subject_name` VARCHAR(100) COMMENT '预算主体名称',
  `apply_amount` DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '申请金额',
  `apply_reason` VARCHAR(500) COMMENT '申请事由',
  `status` VARCHAR(20) DEFAULT 'DRAFT' COMMENT '状态：DRAFT-草稿，PENDING-待审批，APPROVED-已审批，REJECTED-已拒绝',
  `process_definition_id` BIGINT COMMENT '流程定义ID',
  `process_instance_id` BIGINT COMMENT '流程实例ID',
  `applicant_id` BIGINT COMMENT '申请人ID',
  `applicant_code` VARCHAR(20) COMMENT '申请人工号',
  `applicant_name` VARCHAR(50) COMMENT '申请人姓名',
  `dept_id` BIGINT COMMENT '申请人部门ID',
  `dept_code` VARCHAR(20) COMMENT '申请人部门编码',
  `dept_name` VARCHAR(100) COMMENT '申请人部门名称',
  `create_user` VARCHAR(50) COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY `idx_apply_no` (`apply_no`),
  KEY `idx_budget_year` (`budget_year`),
  KEY `idx_item_id` (`item_id`),
  KEY `idx_subject_id` (`subject_id`),
  KEY `idx_status` (`status`),
  KEY `idx_process_instance_id` (`process_instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预算申请表';

-- ==================== 4. 删除不需要的菜单 ====================
-- 删除基础参数设置
DELETE FROM `sys_menu` WHERE `menu_code` IN ('BUDG_PARAM', 'BUDGET_PARAM');

-- 删除预算流程配置
DELETE FROM `sys_menu` WHERE `menu_code` IN ('BUDG_PROCESS', 'BUDGET_PROCESS');

-- 删除预算执行与控制菜单及其子菜单
DELETE FROM `sys_menu` WHERE `menu_code` IN (
  'BUDG_EXECUTION',
  'BUDGET_EXECUTION',
  'BUDG_EXECUTION_DATA',
  'BUDG_EXECUTION_COLLECT',
  'BUDG_AMOUNT_CONTROL',
  'BUDG_EXECUTION_TRACK',
  'BUDG_EXECUTION_MANAGE',
  'BUDGET_EXECUTION_COLLECT',
  'BUDGET_EXECUTION_TRACK'
);

-- ==================== 5. 新增预算综合管理菜单 ====================
SET @budget_menu_id = (SELECT id FROM sys_menu WHERE menu_code = 'BUDGET' LIMIT 1);
SET @budg_menu_id = (SELECT id FROM sys_menu WHERE menu_code = 'BUDG' LIMIT 1);

-- 使用BUDGET菜单（如果存在），否则使用BUDG菜单
SET @parent_menu_id = COALESCE(@budget_menu_id, @budg_menu_id);

-- 预算综合管理（二级菜单）
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('BUDGET_COMPREHENSIVE', '预算综合管理', @parent_menu_id, 1, 'comprehensive', 'Layout', 'el-icon-s-data', 2, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

SET @budget_comprehensive_menu_id = (SELECT id FROM sys_menu WHERE menu_code = 'BUDGET_COMPREHENSIVE');

-- 预算申请
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('BUDGET_APPLY', '预算申请', @budget_comprehensive_menu_id, 2, 'apply', 'budg/BudgetApply', 'el-icon-edit-outline', 1, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 预算审批
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('BUDGET_APPROVAL', '预算审批', @budget_comprehensive_menu_id, 2, 'approval', 'budg/BudgetApproval', 'el-icon-check', 2, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 预算明细
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('BUDGET_DETAIL', '预算明细', @budget_comprehensive_menu_id, 2, 'detail', 'budg/BudgetDetail', 'el-icon-document', 3, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- ==================== 6. 调整预算项目与分类管理为目录并新增子菜单 ====================
SET @budget_basic_menu_id = (SELECT id FROM sys_menu WHERE menu_code = 'BUDGET_BASIC' LIMIT 1);

-- 将预算项目与分类管理改为目录
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('BUDGET_ITEM', '预算项目与分类管理', @budget_basic_menu_id, 1, 'item', 'Layout', 'el-icon-menu', 2, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name), menu_type = VALUES(menu_type), component = VALUES(component);

SET @budget_item_menu_id = (SELECT id FROM sys_menu WHERE menu_code = 'BUDGET_ITEM' LIMIT 1);

-- 分类管理
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('BUDGET_ITEM_CATEGORY', '分类管理', @budget_item_menu_id, 2, 'category', 'budg/BudgetCategory', 'el-icon-collection', 1, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 项目预算
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('BUDGET_ITEM_PROJECT', '项目预算', @budget_item_menu_id, 2, 'project', 'budg/BudgetProject', 'el-icon-s-order', 2, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

