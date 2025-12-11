-- HRP系统初始数据脚本
USE hrp_db;

-- ==================== 系统基础数据 ====================

-- 插入系统参数（原始密码）
INSERT INTO `sys_code` (`id`, `code_type`, `code_type_name`, `code_value`, `code_name`, `is_stop`, `create_user`) VALUES
('RESET_PASSWORD', 'SYSTEM_PARAM', '系统参数', '123456', '原始密码', 0, 'SYSTEM'),
('FORCE_CHANGE_PASSWORD', 'SYSTEM_PARAM', '系统参数', '否', '强制修改密码', 0, 'SYSTEM')
ON DUPLICATE KEY UPDATE 
    code_type_name = VALUES(code_type_name),
    code_name = VALUES(code_name),
    is_stop = 0;

-- 插入代码类型数据
INSERT INTO `sys_code` (`id`, `code_type`, `code_type_name`, `code_value`, `code_name`, `is_stop`, `create_user`) VALUES
-- 流程类型
(UUID(), 'PROCESS_TYPE', '流程类型', 'PAYOUT', '报账审批', 0, 'SYSTEM'),
(UUID(), 'PROCESS_TYPE', '流程类型', 'CONTRACT', '合同审批', 0, 'SYSTEM'),
(UUID(), 'PROCESS_TYPE', '流程类型', 'BUDGET', '预算审批', 0, 'SYSTEM'),
-- 业务类型
(UUID(), 'BUSINESS_TYPE', '业务类型', 'PAYOUT', '报账', 0, 'SYSTEM'),
(UUID(), 'BUSINESS_TYPE', '业务类型', 'CONTRACT', '合同', 0, 'SYSTEM'),
(UUID(), 'BUSINESS_TYPE', '业务类型', 'PROCUREMENT', '采购', 0, 'SYSTEM'),
-- 申请类型
(UUID(), 'PAYOUT_TYPE', '申请类型', 'TRAVEL', '差旅费', 0, 'SYSTEM'),
(UUID(), 'PAYOUT_TYPE', '申请类型', 'OFFICE', '办公费', 0, 'SYSTEM'),
(UUID(), 'PAYOUT_TYPE', '申请类型', 'MEAL', '餐费', 0, 'SYSTEM'),
(UUID(), 'PAYOUT_TYPE', '申请类型', 'TRANSPORT', '交通费', 0, 'SYSTEM'),
(UUID(), 'PAYOUT_TYPE', '申请类型', 'OTHER', '其他', 0, 'SYSTEM'),
-- 合同类型
(UUID(), 'CONTRACT_TYPE', '合同类型', 'PURCHASE', '采购合同', 0, 'SYSTEM'),
(UUID(), 'CONTRACT_TYPE', '合同类型', 'SERVICE', '服务合同', 0, 'SYSTEM'),
(UUID(), 'CONTRACT_TYPE', '合同类型', 'CONSTRUCTION', '工程合同', 0, 'SYSTEM'),
(UUID(), 'CONTRACT_TYPE', '合同类型', 'LEASE', '租赁合同', 0, 'SYSTEM'),
(UUID(), 'CONTRACT_TYPE', '合同类型', 'OTHER', '其他', 0, 'SYSTEM'),
-- 任务状态
(UUID(), 'TASK_STATUS', '任务状态', 'PENDING', '待办理', 0, 'SYSTEM'),
(UUID(), 'TASK_STATUS', '任务状态', 'COMPLETED', '已完成', 0, 'SYSTEM'),
(UUID(), 'TASK_STATUS', '任务状态', 'TRANSFERRED', '已转办', 0, 'SYSTEM'),
-- 流程状态
(UUID(), 'PROCESS_STATUS', '流程状态', 'RUNNING', '运行中', 0, 'SYSTEM'),
(UUID(), 'PROCESS_STATUS', '流程状态', 'COMPLETED', '已完成', 0, 'SYSTEM'),
(UUID(), 'PROCESS_STATUS', '流程状态', 'TERMINATED', '已终止', 0, 'SYSTEM'),
(UUID(), 'PROCESS_STATUS', '流程状态', 'SUSPENDED', '已挂起', 0, 'SYSTEM'),
-- 申请状态
(UUID(), 'APPLY_STATUS', '申请状态', 'DRAFT', '草稿', 0, 'SYSTEM'),
(UUID(), 'APPLY_STATUS', '申请状态', 'PENDING', '待审批', 0, 'SYSTEM'),
(UUID(), 'APPLY_STATUS', '申请状态', 'APPROVED', '已审批', 0, 'SYSTEM'),
(UUID(), 'APPLY_STATUS', '申请状态', 'REJECTED', '已拒绝', 0, 'SYSTEM'),
(UUID(), 'APPLY_STATUS', '申请状态', 'WITHDRAWN', '已撤回', 0, 'SYSTEM')
ON DUPLICATE KEY UPDATE 
    code_type_name = VALUES(code_type_name),
    code_name = VALUES(code_name),
    is_stop = 0;

-- 插入岗位数据（示例）
INSERT INTO `sys_code` (`id`, `code_type`, `code_type_name`, `code_value`, `code_name`, `is_stop`, `create_user`) VALUES
(UUID(), 'POSITION', '岗位', 'DIRECTOR', '主任', 0, 'SYSTEM'),
(UUID(), 'POSITION', '岗位', 'DEPUTY_DIRECTOR', '副主任', 0, 'SYSTEM'),
(UUID(), 'POSITION', '岗位', 'CHIEF_PHYSICIAN', '主任医师', 0, 'SYSTEM'),
(UUID(), 'POSITION', '岗位', 'ATTENDING_PHYSICIAN', '主治医师', 0, 'SYSTEM'),
(UUID(), 'POSITION', '岗位', 'RESIDENT', '住院医师', 0, 'SYSTEM'),
(UUID(), 'POSITION', '岗位', 'NURSE', '护士', 0, 'SYSTEM'),
(UUID(), 'POSITION', '岗位', 'ACCOUNTANT', '会计', 0, 'SYSTEM'),
(UUID(), 'POSITION', '岗位', 'MANAGER', '管理员', 0, 'SYSTEM')
ON DUPLICATE KEY UPDATE 
    code_type_name = VALUES(code_type_name),
    code_name = VALUES(code_name),
    is_stop = 0;

-- ==================== 部门数据 ====================

-- 插入部门数据
INSERT INTO `sys_dept` (`dept_code`, `dept_name`, `super_dept_code`, `dept_level`, `dept_phone`, `is_stop`, `create_user`) VALUES
('DEPT001', '院办', NULL, 1, '010-12345678', 0, 'SYSTEM'),
('DEPT002', '财务科', NULL, 1, '010-12345679', 0, 'SYSTEM'),
('DEPT003', '人事科', NULL, 1, '010-12345680', 0, 'SYSTEM'),
('DEPT004', '内科', NULL, 1, '010-12345681', 0, 'SYSTEM'),
('DEPT005', '外科', NULL, 1, '010-12345682', 0, 'SYSTEM'),
('DEPT006', '消化内科', 'DEPT004', 2, '010-12345683', 0, 'SYSTEM'),
('DEPT007', '心内科', 'DEPT004', 2, '010-12345684', 0, 'SYSTEM'),
('DEPT008', '普外科', 'DEPT005', 2, '010-12345685', 0, 'SYSTEM')
ON DUPLICATE KEY UPDATE 
    dept_name = VALUES(dept_name),
    is_stop = 0;

-- ==================== 职工数据 ====================

-- 插入职工数据
INSERT INTO `sys_emp` (`emp_code`, `emp_name`, `emp_sex`, `emp_phone`, `emp_email`, `dept_code`, `is_stop`, `create_user`) VALUES
('EMP001', '张三', 1, '13800138001', 'zhangsan@hospital.com', 'DEPT001', 0, 'SYSTEM'),
('EMP002', '李四', 2, '13800138002', 'lisi@hospital.com', 'DEPT002', 0, 'SYSTEM'),
('EMP003', '王五', 1, '13800138003', 'wangwu@hospital.com', 'DEPT003', 0, 'SYSTEM'),
('EMP004', '赵六', 1, '13800138004', 'zhaoliu@hospital.com', 'DEPT006', 0, 'SYSTEM'),
('EMP005', '钱七', 2, '13800138005', 'qianqi@hospital.com', 'DEPT007', 0, 'SYSTEM'),
('EMP006', '孙八', 1, '13800138006', 'sunba@hospital.com', 'DEPT008', 0, 'SYSTEM')
ON DUPLICATE KEY UPDATE 
    emp_name = VALUES(emp_name),
    is_stop = 0;

-- ==================== 角色数据 ====================

-- 插入角色数据
INSERT INTO `sys_role` (`role_code`, `role_name`, `role_desc`, `is_stop`, `create_user`) VALUES
('ROLE_ADMIN', '系统管理员', '系统管理员，拥有所有权限', 0, 'SYSTEM'),
('ROLE_FINANCE', '财务人员', '财务人员，负责财务相关业务', 0, 'SYSTEM'),
('ROLE_DEPT_MANAGER', '部门负责人', '部门负责人，负责部门管理', 0, 'SYSTEM'),
('ROLE_EMPLOYEE', '普通员工', '普通员工，基础权限', 0, 'SYSTEM')
ON DUPLICATE KEY UPDATE 
    role_name = VALUES(role_name),
    is_stop = 0;

-- ==================== 菜单数据 ====================

-- 插入系统平台菜单
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) VALUES
('SYSTEM', '系统平台', 0, 1, '/system', 'Layout', 'el-icon-setting', 1, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 获取系统平台菜单ID
SET @system_menu_id = (SELECT id FROM sys_menu WHERE menu_code = 'SYSTEM');

-- 管理平台（二级菜单）
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('SYSTEM_MANAGE', '管理平台', @system_menu_id, 1, 'manage', 'Layout', 'el-icon-s-tools', 1, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

SET @manage_menu_id = (SELECT id FROM sys_menu WHERE menu_code = 'SYSTEM_MANAGE');

-- 用户管理
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('SYSTEM_USER', '用户管理', @manage_menu_id, 2, 'system/user', 'system/UserManagement', 'el-icon-user', 1, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 权限管理
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('SYSTEM_PERMISSION', '权限管理', @manage_menu_id, 2, 'system/permission', 'system/PermissionManagement', 'el-icon-lock', 2, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 部门管理
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('SYSTEM_DEPT', '部门管理', @manage_menu_id, 2, 'system/dept', 'system/DeptManagement', 'el-icon-office-building', 3, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 岗位管理
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('SYSTEM_POSITION', '岗位管理', @manage_menu_id, 2, 'system/position', 'system/PositionManagement', 'el-icon-s-custom', 4, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 职工管理
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('SYSTEM_EMPLOYEE', '职工管理', @manage_menu_id, 2, 'system/employee', 'system/EmployeeManagement', 'el-icon-user-solid', 5, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 系统设置（二级菜单）
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('SYSTEM_SETTINGS', '系统设置', @system_menu_id, 1, 'settings', 'Layout', 'el-icon-setting', 2, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

SET @settings_menu_id = (SELECT id FROM sys_menu WHERE menu_code = 'SYSTEM_SETTINGS');

-- 系统参数
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('SYSTEM_PARAMS', '系统参数', @settings_menu_id, 2, 'system/params', 'system/SystemParams', 'el-icon-setting', 1, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 业务平台（二级菜单）
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('SYSTEM_BUSINESS', '业务平台', @system_menu_id, 1, 'business', 'Layout', 'el-icon-s-operation', 3, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

SET @business_menu_id = (SELECT id FROM sys_menu WHERE menu_code = 'SYSTEM_BUSINESS');

-- 流程定义
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('BUSINESS_PROCESS_DEFINITION', '流程定义', @business_menu_id, 2, 'business/process-definition', 'business/ProcessDefinition', 'el-icon-edit-outline', 1, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 流程任务
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('BUSINESS_PROCESS_TASK', '流程任务', @business_menu_id, 2, 'business/process-task', 'business/ProcessTask', 'el-icon-tickets', 2, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 流程实例
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('BUSINESS_PROCESS_INSTANCE', '流程实例', @business_menu_id, 2, 'business/process-instance', 'business/ProcessInstance', 'el-icon-view', 3, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- ==================== 智能报账模块菜单 ====================

INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) VALUES
('REIMB', '智能报账', 0, 1, '/reimb', 'Layout', 'el-icon-money', 2, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

SET @reimb_menu_id = (SELECT id FROM sys_menu WHERE menu_code = 'REIMB');

-- 业务办理（二级菜单）
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('REIMB_BUSINESS', '业务办理', @reimb_menu_id, 1, 'business', 'Layout', 'el-icon-s-order', 1, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

SET @reimb_business_menu_id = (SELECT id FROM sys_menu WHERE menu_code = 'REIMB_BUSINESS');

-- 我的申请
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('REIMB_MY_APPLY', '我的申请', @reimb_business_menu_id, 2, 'my-apply', 'reimb/MyReimbApply', 'el-icon-document', 1, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 我的报账
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('REIMB_MY_PAYOUT', '我的报账', @reimb_business_menu_id, 2, 'my-payout', 'reimb/MyReimbPayout', 'el-icon-wallet', 2, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 业务审批（二级菜单）
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('REIMB_APPROVAL', '业务审批', @reimb_menu_id, 1, 'approval', 'Layout', 'el-icon-check', 2, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

SET @reimb_approval_menu_id = (SELECT id FROM sys_menu WHERE menu_code = 'REIMB_APPROVAL');

-- 申请审批
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('REIMB_APPLY_APPROVAL', '申请审批', @reimb_approval_menu_id, 2, 'apply-approval', 'reimb/ApplyApproval', 'el-icon-document-checked', 1, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 报账审批
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('REIMB_PAYOUT_APPROVAL', '报账审批', @reimb_approval_menu_id, 2, 'payout-approval', 'reimb/ReimbApproval', 'el-icon-wallet', 2, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 财务处理（二级菜单）
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('REIMB_FINANCE', '财务处理', @reimb_menu_id, 1, 'finance', 'Layout', 'el-icon-s-data', 3, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

SET @reimb_finance_menu_id = (SELECT id FROM sys_menu WHERE menu_code = 'REIMB_FINANCE');

-- 申请查询
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('REIMB_APPLY_QUERY', '申请查询', @reimb_finance_menu_id, 2, 'apply-query', 'reimb/ApplyQuery', 'el-icon-search', 1, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 报账查询
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('REIMB_PAYOUT_QUERY', '报账查询', @reimb_finance_menu_id, 2, 'payout-query', 'reimb/ReimbQuery', 'el-icon-search', 2, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- ==================== 预算管理模块菜单 ====================

INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) VALUES
('BUDG', '预算管理', 0, 1, '/budg', 'Layout', 'el-icon-coin', 4, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

SET @budg_menu_id = (SELECT id FROM sys_menu WHERE menu_code = 'BUDG');

-- 预算基础设置（二级菜单）
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('BUDG_BASIC', '预算基础设置', @budg_menu_id, 1, 'basic', 'Layout', 'el-icon-setting', 1, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

SET @budg_basic_menu_id = (SELECT id FROM sys_menu WHERE menu_code = 'BUDG_BASIC');

-- 预算主体管理
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('BUDG_SUBJECT', '预算主体管理', @budg_basic_menu_id, 2, 'subject', 'budg/BudgetSubject', 'el-icon-office-building', 1, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 预算项目与分类管理
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('BUDG_ITEM', '预算项目与分类管理', @budg_basic_menu_id, 2, 'item', 'budg/BudgetItem', 'el-icon-menu', 2, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 预算流程配置
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('BUDG_PROCESS', '预算流程配置', @budg_basic_menu_id, 2, 'process', 'budg/BudgetProcess', 'el-icon-connection', 3, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 基础参数设置
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('BUDG_PARAM', '基础参数设置', @budg_basic_menu_id, 2, 'param', 'budg/BudgetParam', 'el-icon-tools', 4, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 预算执行与控制（二级菜单）
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('BUDG_EXECUTION', '预算执行与控制', @budg_menu_id, 1, 'execution', 'Layout', 'el-icon-data-line', 2, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

SET @budg_execution_menu_id = (SELECT id FROM sys_menu WHERE menu_code = 'BUDG_EXECUTION');

-- 执行数据采集
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('BUDG_EXECUTION_DATA', '执行数据采集', @budg_execution_menu_id, 2, 'execution-data', 'budg/BudgetExecutionData', 'el-icon-upload', 1, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 实时额度控制
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('BUDG_AMOUNT_CONTROL', '实时额度控制', @budg_execution_menu_id, 2, 'amount-control', 'budg/BudgetAmountControl', 'el-icon-warning', 2, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 执行进度跟踪与查询
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('BUDG_EXECUTION_TRACK', '执行进度跟踪与查询', @budg_execution_menu_id, 2, 'execution-track', 'budg/BudgetExecutionTrack', 'el-icon-data-analysis', 3, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 执行单管理
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('BUDG_EXECUTION_MANAGE', '执行单管理', @budg_execution_menu_id, 2, 'execution-manage', 'budg/BudgetExecutionManage', 'el-icon-tickets', 4, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 预算调整与分析（二级菜单）
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('BUDG_ADJUSTMENT', '预算调整与分析', @budg_menu_id, 1, 'adjustment', 'Layout', 'el-icon-edit-outline', 3, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

SET @budg_adjustment_menu_id = (SELECT id FROM sys_menu WHERE menu_code = 'BUDG_ADJUSTMENT');

-- 预算调整管理
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('BUDG_ADJUSTMENT_MANAGE', '预算调整管理', @budg_adjustment_menu_id, 2, 'adjustment-manage', 'budg/BudgetAdjustmentManage', 'el-icon-edit', 1, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 预算执行分析
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('BUDG_ANALYSIS', '预算执行分析', @budg_adjustment_menu_id, 2, 'analysis', 'budg/BudgetAnalysis', 'el-icon-pie-chart', 2, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- ==================== 合同管理模块菜单 ====================

INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) VALUES
('CONTRACT', '合同管理', 0, 1, '/contract', 'Layout', 'el-icon-document-copy', 3, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

SET @contract_menu_id = (SELECT id FROM sys_menu WHERE menu_code = 'CONTRACT');

-- 工作台
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('CONTRACT_WORKBENCH', '工作台', @contract_menu_id, 2, 'workbench', 'contract/ContractWorkbench', 'el-icon-s-grid', 1, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 合同起草
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('CONTRACT_DRAFT', '合同起草', @contract_menu_id, 2, 'draft', 'contract/ContractDraft', 'el-icon-edit', 2, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 合同审批
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('CONTRACT_APPROVAL', '合同审批', @contract_menu_id, 2, 'approval', 'contract/ContractApproval', 'el-icon-check', 3, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 合同执行
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('CONTRACT_EXECUTION', '合同执行', @contract_menu_id, 2, 'execution', 'contract/ContractExecution', 'el-icon-s-operation', 4, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- ==================== 预算管理模块菜单 ====================

INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) VALUES
('BUDGET', '预算管理', 0, 1, '/budget', 'Layout', 'el-icon-s-finance', 4, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

SET @budget_menu_id = (SELECT id FROM sys_menu WHERE menu_code = 'BUDGET');

-- 预算基础设置
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('BUDGET_BASIC', '预算基础设置', @budget_menu_id, 1, 'basic', 'Layout', 'el-icon-setting', 1, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

SET @budget_basic_menu_id = (SELECT id FROM sys_menu WHERE menu_code = 'BUDGET_BASIC');

-- 预算主体管理
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('BUDGET_SUBJECT', '预算主体管理', @budget_basic_menu_id, 2, 'subject', 'budget/BudgetSubject', 'el-icon-s-custom', 1, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 预算项目与分类管理
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('BUDGET_ITEM', '预算项目与分类管理', @budget_basic_menu_id, 2, 'item', 'budget/BudgetItem', 'el-icon-menu', 2, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 预算流程配置
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('BUDGET_PROCESS', '预算流程配置', @budget_basic_menu_id, 2, 'process', 'budget/BudgetProcess', 'el-icon-s-order', 3, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 基础参数设置
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('BUDGET_PARAM', '基础参数设置', @budget_basic_menu_id, 2, 'param', 'budget/BudgetParam', 'el-icon-setting', 4, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 预算执行与控制
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('BUDGET_EXECUTION', '预算执行与控制', @budget_menu_id, 1, 'execution', 'Layout', 'el-icon-s-marketing', 2, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

SET @budget_execution_menu_id = (SELECT id FROM sys_menu WHERE menu_code = 'BUDGET_EXECUTION');

-- 执行数据采集
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('BUDGET_EXECUTION_COLLECT', '执行数据采集', @budget_execution_menu_id, 2, 'collect', 'budget/BudgetExecutionCollect', 'el-icon-upload', 1, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 执行进度跟踪
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('BUDGET_EXECUTION_TRACK', '执行进度跟踪', @budget_execution_menu_id, 2, 'track', 'budget/BudgetExecutionTrack', 'el-icon-data-line', 2, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 执行单管理
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('BUDGET_EXECUTION_MANAGE', '执行单管理', @budget_execution_menu_id, 2, 'manage', 'budget/BudgetExecutionManage', 'el-icon-s-order', 3, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 预算调整与分析
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('BUDGET_ANALYSIS', '预算调整与分析', @budget_menu_id, 1, 'analysis', 'Layout', 'el-icon-data-analysis', 3, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

SET @budget_analysis_menu_id = (SELECT id FROM sys_menu WHERE menu_code = 'BUDGET_ANALYSIS');

-- 预算调整管理
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('BUDGET_ADJUSTMENT', '预算调整管理', @budget_analysis_menu_id, 2, 'adjustment', 'budget/BudgetAdjustment', 'el-icon-edit', 1, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 预算执行分析
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('BUDGET_ANALYSIS_REPORT', '预算执行分析', @budget_analysis_menu_id, 2, 'report', 'budget/BudgetAnalysisReport', 'el-icon-s-data', 2, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- ==================== 固定资产模块菜单 ====================

INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) VALUES
('ASSET', '固定资产', 0, 1, '/asset', 'Layout', 'el-icon-box', 5, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

SET @asset_menu_id = (SELECT id FROM sys_menu WHERE menu_code = 'ASSET');

-- 业务审批（目录）
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('ASSET_APPROVAL', '业务审批', @asset_menu_id, 1, 'approval', 'Layout', 'el-icon-check', 1, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

SET @asset_approval_menu_id = (SELECT id FROM sys_menu WHERE menu_code = 'ASSET_APPROVAL');

-- 资产调拨审批
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('ASSET_APPROVAL_TRANSFER', '资产调拨审批', @asset_approval_menu_id, 2, 'transfer', 'asset/AssetApproval', 'el-icon-sort', 1, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 资产处置审批
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('ASSET_APPROVAL_DISPOSAL', '资产处置审批', @asset_approval_menu_id, 2, 'disposal', 'asset/AssetApproval', 'el-icon-delete', 2, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 盘点差异审批
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('ASSET_APPROVAL_INVENTORY', '盘点差异审批', @asset_approval_menu_id, 2, 'inventory', 'asset/AssetApproval', 'el-icon-warning', 3, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 资产变动审批
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('ASSET_APPROVAL_CHANGE', '资产变动审批', @asset_approval_menu_id, 2, 'change', 'asset/AssetApproval', 'el-icon-edit', 4, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 采购需求（目录）
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('ASSET_PROCUREMENT', '采购需求', @asset_menu_id, 1, 'procurement', 'Layout', 'el-icon-shopping-cart-full', 2, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

SET @asset_procurement_menu_id = (SELECT id FROM sys_menu WHERE menu_code = 'ASSET_PROCUREMENT');

-- 需求发起
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('ASSET_PROCUREMENT_APPLY', '需求发起', @asset_procurement_menu_id, 2, 'apply', 'asset/ProcurementApply', 'el-icon-plus', 1, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 需求审核
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('ASSET_PROCUREMENT_APPROVAL', '需求审核', @asset_procurement_menu_id, 2, 'approval', 'asset/ProcurementApproval', 'el-icon-check', 2, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 综合查询（目录）
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('ASSET_QUERY', '综合查询', @asset_menu_id, 1, 'query', 'Layout', 'el-icon-search', 3, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

SET @asset_query_menu_id = (SELECT id FROM sys_menu WHERE menu_code = 'ASSET_QUERY');

-- 资产查询
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('ASSET_QUERY_ASSET', '资产查询', @asset_query_menu_id, 2, 'asset', 'asset/AssetQuery', 'el-icon-box', 1, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- 采购查询
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('ASSET_QUERY_PROCUREMENT', '采购查询', @asset_query_menu_id, 2, 'procurement-query', 'asset/ProcurementQuery', 'el-icon-shopping-cart-2', 2, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- ==================== 全景人力模块菜单 ====================

INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) VALUES
('HR', '全景人力', 0, 1, '/hr', 'Layout', 'el-icon-user-solid', 6, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

SET @hr_menu_id = (SELECT id FROM sys_menu WHERE menu_code = 'HR');

INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('HR_SALARY', '人员薪酬', @hr_menu_id, 2, 'salary', 'hr/SalaryManagement', 'el-icon-coin', 1, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('HR_SALARY_CALC', '薪酬计算', @hr_menu_id, 2, 'salary-calc', 'hr/SalaryCalculation', 'el-icon-cpu', 2, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- ==================== DIP成本模块菜单 ====================

INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) VALUES
('COST', 'DIP成本', 0, 1, '/cost', 'Layout', 'el-icon-pie-chart', 7, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

SET @cost_menu_id = (SELECT id FROM sys_menu WHERE menu_code = 'COST');

INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('COST_REPORT', '成本报表', @cost_menu_id, 2, 'report', 'cost/CostReport', 'el-icon-document', 1, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('COST_ANALYSIS', '成本分析', @cost_menu_id, 2, 'analysis', 'cost/CostAnalysis', 'el-icon-data-analysis', 2, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('COST_ACCOUNTING', '成本核算', @cost_menu_id, 2, 'accounting', 'cost/CostAccounting', 'el-icon-s-data', 3, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- ==================== 单机效能模块菜单 ====================

INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) VALUES
('EFFICIENCY', '单机效能', 0, 1, '/efficiency', 'Layout', 'el-icon-monitor', 8, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

SET @efficiency_menu_id = (SELECT id FROM sys_menu WHERE menu_code = 'EFFICIENCY');

INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('EFFICIENCY_COLLECTION', '数据采集', @efficiency_menu_id, 2, 'collection', 'efficiency/DataCollection', 'el-icon-upload', 1, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('EFFICIENCY_INCOME', '收入数据', @efficiency_menu_id, 2, 'income', 'efficiency/IncomeData', 'el-icon-coin', 2, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('EFFICIENCY_COST', '成本数据', @efficiency_menu_id, 2, 'cost', 'efficiency/CostData', 'el-icon-wallet', 3, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('EFFICIENCY_EQUIPMENT_REPORT', '设备分析报告', @efficiency_menu_id, 2, 'equipment-report', 'efficiency/EquipmentAnalysisReport', 'el-icon-document', 4, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('EFFICIENCY_INVESTMENT', '投资收益分析', @efficiency_menu_id, 2, 'investment', 'efficiency/InvestmentReturnAnalysis', 'el-icon-trophy', 5, 1)
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- ==================== 预算管理初始数据 ====================

-- 插入预算项目分类数据
INSERT INTO `budget_category` (`category_code`, `category_name`, `category_type`, `category_level`, `is_stop`, `create_user`) VALUES
-- 收入预算
('INCOME_001', '门诊收入', 'INCOME', 1, 0, 'SYSTEM'),
('INCOME_002', '住院收入', 'INCOME', 1, 0, 'SYSTEM'),
('INCOME_003', '其他收入', 'INCOME', 1, 0, 'SYSTEM'),
-- 支出预算
('EXPENSE_001', '设备采购', 'EXPENSE', 1, 0, 'SYSTEM'),
('EXPENSE_002', '药品采购', 'EXPENSE', 1, 0, 'SYSTEM'),
('EXPENSE_003', '办公费', 'EXPENSE', 1, 0, 'SYSTEM'),
('EXPENSE_004', '差旅费', 'EXPENSE', 1, 0, 'SYSTEM'),
('EXPENSE_005', '其他支出', 'EXPENSE', 1, 0, 'SYSTEM'),
-- 专项预算
('SPECIAL_001', '财政拨款', 'SPECIAL', 1, 0, 'SYSTEM'),
('SPECIAL_002', '科研经费', 'SPECIAL', 1, 0, 'SYSTEM')
ON DUPLICATE KEY UPDATE 
    category_name = VALUES(category_name),
    is_stop = 0;

-- 插入预算项目数据
INSERT INTO `budget_item` (`item_code`, `item_name`, `category_id`, `category_code`, `account_subject`, `is_centralized`, `allow_adjust`, `is_stop`, `create_user`) 
SELECT 
    'ITEM_001', '医疗设备采购', category_id, category_code, '固定资产-医疗设备', 1, 1, 0, 'SYSTEM'
FROM budget_category WHERE category_code = 'EXPENSE_001'
ON DUPLICATE KEY UPDATE item_name = VALUES(item_name);

INSERT INTO `budget_item` (`item_code`, `item_name`, `category_id`, `category_code`, `account_subject`, `is_centralized`, `allow_adjust`, `is_stop`, `create_user`) 
SELECT 
    'ITEM_002', '办公用品采购', category_id, category_code, '管理费用-办公费', 0, 1, 0, 'SYSTEM'
FROM budget_category WHERE category_code = 'EXPENSE_003'
ON DUPLICATE KEY UPDATE item_name = VALUES(item_name);

INSERT INTO `budget_item` (`item_code`, `item_name`, `category_id`, `category_code`, `account_subject`, `is_centralized`, `allow_adjust`, `is_stop`, `create_user`) 
SELECT 
    'ITEM_003', '差旅费报销', category_id, category_code, '管理费用-差旅费', 0, 1, 0, 'SYSTEM'
FROM budget_category WHERE category_code = 'EXPENSE_004'
ON DUPLICATE KEY UPDATE item_name = VALUES(item_name);

-- 插入预算参数数据
INSERT INTO `budget_param` (`param_code`, `param_name`, `param_value`, `param_type`, `param_desc`, `is_stop`, `create_user`) VALUES
('BUDGET_PERIOD', '预算周期', 'YEAR', 'PERIOD', '预算编制周期：YEAR-年度，QUARTER-季度，MONTH-月度', 0, 'SYSTEM'),
('WARNING_THRESHOLD', '预警阈值', '90', 'WARNING', '预算执行率预警阈值（%）', 0, 'SYSTEM'),
('INTERCEPT_THRESHOLD', '拦截阈值', '100', 'INTERCEPT', '预算超支拦截阈值（%）', 0, 'SYSTEM'),
('INTERCEPT_RULE', '拦截规则', '10', 'INTERCEPT', '超支拦截规则：超支10%以上自动拦截', 0, 'SYSTEM')
ON DUPLICATE KEY UPDATE 
    param_name = VALUES(param_name),
    param_value = VALUES(param_value),
    is_stop = 0;

-- ==================== 示例预算数据 ====================

-- 插入示例预算主体
INSERT INTO `budget_subject` (`subject_code`, `subject_name`, `subject_type`, `dept_code`, `subject_level`, `is_stop`, `create_user`) VALUES
('SUBJECT_001', '消化内科', 'CLINIC', 'DEPT006', 2, 0, 'SYSTEM'),
('SUBJECT_002', '心内科', 'CLINIC', 'DEPT007', 2, 0, 'SYSTEM'),
('SUBJECT_003', '财务科', 'ADMIN', 'DEPT002', 1, 0, 'SYSTEM')
ON DUPLICATE KEY UPDATE 
    subject_name = VALUES(subject_name),
    is_stop = 0;

-- 插入示例预算（2024年度）
INSERT INTO `budget` (`budget_no`, `budget_name`, `budget_year`, `budget_period`, `subject_id`, `subject_code`, `item_id`, `item_code`, `budget_amount`, `executed_amount`, `remaining_amount`, `execution_rate`, `status`, `create_user`) 
SELECT 
    'BUDGET2024001', '2024年度办公费预算', '2024', 'YEAR', 
    subject_id, subject_code,
    item_id, item_code,
    100000.00, 0.00, 100000.00, 0.00, 'APPROVED', 'SYSTEM'
FROM budget_subject, budget_item 
WHERE budget_subject.subject_code = 'SUBJECT_001' 
  AND budget_item.item_code = 'ITEM_002'
LIMIT 1
ON DUPLICATE KEY UPDATE budget_name = VALUES(budget_name);
