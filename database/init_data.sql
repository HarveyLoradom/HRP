-- HRP系统初始数据脚本
USE hrp_db;

-- 插入初始管理员用户（密码：123456，使用BCrypt加密）
-- 注意：id使用UUID，请运行PasswordGenerator工具生成新的UUID和密码哈希值
-- 运行命令：cd backward/hrp-auth && mvn compile exec:java -Dexec.mainClass="com.hrp.auth.util.PasswordGenerator"
-- 然后将生成的SQL语句替换下面的内容

-- 示例（请使用PasswordGenerator生成实际的UUID和密码哈希）：
-- INSERT INTO `sys_user` (`id`, `account`, `name`, `type`, `password`, `is_stop`) VALUES
-- ('生成的UUID', 'ADMIN001', '系统管理员', 1, '生成的BCrypt哈希值', 0);

-- 插入用户登录信息（user_id需要与上面的id一致）
-- INSERT INTO `sys_user_login` (`user_id`, `account`, `locked`, `login_fail_count`) VALUES
-- ('生成的UUID', 'ADMIN001', 0, 0);

-- 插入系统菜单
-- 先插入父菜单（系统平台）
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) VALUES
('SYSTEM', '系统平台', 0, 1, '/system', 'Layout', 'el-icon-setting', 1, 1);

-- 获取系统平台菜单ID
SET @system_menu_id = (SELECT id FROM sys_menu WHERE menu_code = 'SYSTEM');

-- ==================== 管理平台 ====================
-- 用户管理
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('SYSTEM_USER', '用户管理', @system_menu_id, 2, 'system/user', 'system/UserManagement', 'el-icon-user', 1, 1);

-- 权限管理
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('SYSTEM_PERMISSION', '权限管理', @system_menu_id, 2, 'system/permission', 'system/PermissionManagement', 'el-icon-lock', 2, 1);

-- 部门管理
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('SYSTEM_DEPT', '部门管理', @system_menu_id, 2, 'system/dept', 'system/DeptManagement', 'el-icon-office-building', 3, 1);

-- 岗位管理
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('SYSTEM_POSITION', '岗位管理', @system_menu_id, 2, 'system/position', 'system/PositionManagement', 'el-icon-s-custom', 4, 1);

-- 职工管理
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('SYSTEM_EMPLOYEE', '职工管理', @system_menu_id, 2, 'system/employee', 'system/EmployeeManagement', 'el-icon-user-solid', 5, 1);

-- ==================== 系统设置 ====================
-- 系统参数（关联sys_code表）
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('SYSTEM_PARAMS', '系统参数', @system_menu_id, 2, 'system/params', 'system/SystemParams', 'el-icon-setting', 6, 1);

-- ==================== 业务平台 ====================
-- 业务平台（占位，菜单后续再改）
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
VALUES ('SYSTEM_BUSINESS', '业务平台', @system_menu_id, 2, 'system/business', 'system/BusinessPlatform', 'el-icon-s-operation', 7, 1);

-- 智能报账模块菜单
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) VALUES
('REIMB', '智能报账', 0, 1, '/reimb', 'Layout', 'el-icon-money', 2, 1);

INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
SELECT 'REIMB_MY_APPLY', '我的申请', id, 2, 'my-apply', 'reimb/MyReimbApply', 'el-icon-document', 1, 1
FROM sys_menu WHERE menu_code = 'REIMB';

INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
SELECT 'REIMB_APPROVAL', '申请审批', id, 2, 'approval', 'reimb/ReimbApproval', 'el-icon-check', 2, 1
FROM sys_menu WHERE menu_code = 'REIMB';

INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
SELECT 'REIMB_QUERY', '报账查询', id, 2, 'query', 'reimb/ReimbQuery', 'el-icon-search', 3, 1
FROM sys_menu WHERE menu_code = 'REIMB';

-- 合同管理模块菜单
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) VALUES
('CONTRACT', '合同管理', 0, 1, '/contract', 'Layout', 'el-icon-document-copy', 3, 1);

INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
SELECT 'CONTRACT_WORKBENCH', '工作台', id, 2, 'workbench', 'contract/ContractWorkbench', 'el-icon-s-grid', 1, 1
FROM sys_menu WHERE menu_code = 'CONTRACT';

INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
SELECT 'CONTRACT_DRAFT', '合同起草', id, 2, 'draft', 'contract/ContractDraft', 'el-icon-edit', 2, 1
FROM sys_menu WHERE menu_code = 'CONTRACT';

INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
SELECT 'CONTRACT_APPROVAL', '合同审批', id, 2, 'approval', 'contract/ContractApproval', 'el-icon-check', 3, 1
FROM sys_menu WHERE menu_code = 'CONTRACT';

INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
SELECT 'CONTRACT_EXECUTION', '合同执行', id, 2, 'execution', 'contract/ContractExecution', 'el-icon-s-operation', 4, 1
FROM sys_menu WHERE menu_code = 'CONTRACT';

-- 固定资产模块菜单
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) VALUES
('ASSET', '固定资产', 0, 1, '/asset', 'Layout', 'el-icon-box', 4, 1);

INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
SELECT 'ASSET_APPROVAL', '业务审批', id, 2, 'approval', 'asset/AssetApproval', 'el-icon-check', 1, 1
FROM sys_menu WHERE menu_code = 'ASSET';

INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
SELECT 'ASSET_PROCUREMENT', '采购需求', id, 2, 'procurement', 'asset/ProcurementRequirement', 'el-icon-shopping-cart-full', 2, 1
FROM sys_menu WHERE menu_code = 'ASSET';

INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
SELECT 'ASSET_QUERY', '综合查询', id, 2, 'query', 'asset/AssetQuery', 'el-icon-search', 3, 1
FROM sys_menu WHERE menu_code = 'ASSET';

-- 全景人力模块菜单
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) VALUES
('HR', '全景人力', 0, 1, '/hr', 'Layout', 'el-icon-user-solid', 5, 1);

INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
SELECT 'HR_SALARY', '人员薪酬', id, 2, 'salary', 'hr/SalaryManagement', 'el-icon-coin', 1, 1
FROM sys_menu WHERE menu_code = 'HR';

INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
SELECT 'HR_SALARY_CALC', '薪酬计算', id, 2, 'salary-calc', 'hr/SalaryCalculation', 'el-icon-cpu', 2, 1
FROM sys_menu WHERE menu_code = 'HR';

-- DIP成本模块菜单
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) VALUES
('COST', 'DIP成本', 0, 1, '/cost', 'Layout', 'el-icon-pie-chart', 6, 1);

INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
SELECT 'COST_REPORT', '成本报表', id, 2, 'report', 'cost/CostReport', 'el-icon-document', 1, 1
FROM sys_menu WHERE menu_code = 'COST';

INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
SELECT 'COST_ANALYSIS', '成本分析', id, 2, 'analysis', 'cost/CostAnalysis', 'el-icon-data-analysis', 2, 1
FROM sys_menu WHERE menu_code = 'COST';

INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
SELECT 'COST_ACCOUNTING', '成本核算', id, 2, 'accounting', 'cost/CostAccounting', 'el-icon-s-data', 3, 1
FROM sys_menu WHERE menu_code = 'COST';

-- 单机效能模块菜单
INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) VALUES
('EFFICIENCY', '单机效能', 0, 1, '/efficiency', 'Layout', 'el-icon-monitor', 7, 1);

INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
SELECT 'EFFICIENCY_COLLECTION', '数据采集', id, 2, 'collection', 'efficiency/DataCollection', 'el-icon-upload', 1, 1
FROM sys_menu WHERE menu_code = 'EFFICIENCY';

INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
SELECT 'EFFICIENCY_INCOME', '收入数据', id, 2, 'income', 'efficiency/IncomeData', 'el-icon-coin', 2, 1
FROM sys_menu WHERE menu_code = 'EFFICIENCY';

INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
SELECT 'EFFICIENCY_COST', '成本数据', id, 2, 'cost', 'efficiency/CostData', 'el-icon-wallet', 3, 1
FROM sys_menu WHERE menu_code = 'EFFICIENCY';

INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
SELECT 'EFFICIENCY_EQUIPMENT_REPORT', '设备分析报告', id, 2, 'equipment-report', 'efficiency/EquipmentAnalysisReport', 'el-icon-document', 4, 1
FROM sys_menu WHERE menu_code = 'EFFICIENCY';

INSERT INTO `sys_menu` (`menu_code`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `icon`, `sort`, `status`) 
SELECT 'EFFICIENCY_INVESTMENT', '投资收益分析', id, 2, 'investment', 'efficiency/InvestmentReturnAnalysis', 'el-icon-trophy', 5, 1
FROM sys_menu WHERE menu_code = 'EFFICIENCY';

-- 插入系统参数（原始密码）
INSERT INTO `sys_code` (`id`, `code_type`, `code_type_name`, `code_value`, `code_name`, `is_stop`, `create_user`) VALUES
('RESET_PASSWORD', 'SYSTEM_PARAM', '系统参数', '123456', '原始密码', 0, 'SYSTEM');

-- 为管理员分配所有菜单权限
-- 注意：需要将'ADMIN001'替换为实际生成的UUID
-- INSERT INTO `sys_user_menu` (`user_id`, `menu_id`) 
-- SELECT '生成的UUID', id FROM sys_menu WHERE status = 1;
