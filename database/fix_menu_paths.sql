-- ==================== 修复菜单路径的 UPDATE 语句 ====================
-- 说明：根据前端路由配置，修正数据库中菜单的 path 字段
-- 路由守卫逻辑：如果 path 不以 / 开头，会自动拼接 /hrp/
-- 执行前请先备份数据库！

-- ==================== 全景人力模块 ====================
-- 业务申请：数据库是 '/hrp/hr/business-apply'，前端路由是 '/hrp/hr/business-apply'
-- 但根据路由守卫逻辑，应该改为相对路径 'hr/business-apply'
UPDATE `sys_menu` SET `path` = 'hr/business-apply' WHERE `menu_code` = 'HR_BUSINESS_APPLY';

-- 考勤管理：数据库是 '/hrp/hr/attendance/manage'，应该改为 'hr/attendance/manage'
UPDATE `sys_menu` SET `path` = 'hr/attendance/manage' WHERE `menu_code` = 'HR_ATTENDANCE_MANAGE';

-- 请假管理：数据库是 '/hrp/hr/attendance/leave'，应该改为 'hr/attendance/leave'
UPDATE `sys_menu` SET `path` = 'hr/attendance/leave' WHERE `menu_code` = 'HR_ATTENDANCE_LEAVE';

-- 薪酬管理：数据库是 '/hrp/hr/salary'，应该改为 'hr/salary'
UPDATE `sys_menu` SET `path` = 'hr/salary' WHERE `menu_code` = 'HR_SALARY';

-- 绩效管理：数据库是 '/hrp/hr/performance'，应该改为 'hr/performance'
UPDATE `sys_menu` SET `path` = 'hr/performance' WHERE `menu_code` = 'HR_PERFORMANCE';

-- 入转调离管理：数据库是 '/hrp/hr/transfer'，应该改为 'hr/transfer'
UPDATE `sys_menu` SET `path` = 'hr/transfer' WHERE `menu_code` = 'HR_TRANSFER';

-- ==================== DIP成本模块 ====================
-- 成本报表：数据库是 '/hrp/cost/report'，应该改为 'cost/report'
UPDATE `sys_menu` SET `path` = 'cost/report' WHERE `menu_code` = 'COST_REPORT';

-- 成本分析：数据库是 '/hrp/cost/analysis'，应该改为 'cost/analysis'
UPDATE `sys_menu` SET `path` = 'cost/analysis' WHERE `menu_code` = 'COST_ANALYSIS';

-- 成本核算：数据库是 '/hrp/cost/accounting'，应该改为 'cost/accounting'
UPDATE `sys_menu` SET `path` = 'cost/accounting' WHERE `menu_code` = 'COST_ACCOUNTING';

-- ==================== 单机效能模块 ====================
-- 数据采集：数据库是 '/hrp/efficiency/collection'，应该改为 'efficiency/collection'
UPDATE `sys_menu` SET `path` = 'efficiency/collection' WHERE `menu_code` = 'EFFICIENCY_COLLECTION';

-- 收入数据：数据库是 '/hrp/efficiency/income'，应该改为 'efficiency/income'
UPDATE `sys_menu` SET `path` = 'efficiency/income' WHERE `menu_code` = 'EFFICIENCY_INCOME';

-- 成本数据：数据库是 '/hrp/efficiency/cost'，应该改为 'efficiency/cost'
UPDATE `sys_menu` SET `path` = 'efficiency/cost' WHERE `menu_code` = 'EFFICIENCY_COST';

-- 设备分析报告：数据库是 '/hrp/efficiency/equipment-report'，应该改为 'efficiency/equipment-report'
UPDATE `sys_menu` SET `path` = 'efficiency/equipment-report' WHERE `menu_code` = 'EFFICIENCY_EQUIPMENT_REPORT';

-- 投资收益分析：数据库是 '/hrp/efficiency/investment'，应该改为 'efficiency/investment'
UPDATE `sys_menu` SET `path` = 'efficiency/investment' WHERE `menu_code` = 'EFFICIENCY_INVESTMENT';

-- ==================== 预算管理模块 ====================
-- 预算主体管理：数据库是 'subject'，前端路由是 '/hrp/budg/subject'，应该改为 'budg/subject'
UPDATE `sys_menu` SET `path` = 'budg/subject' WHERE `menu_code` = 'BUDG_SUBJECT';

-- 预算项目与分类管理：数据库是 'item'，前端路由是 '/hrp/budg/item'，应该改为 'budg/item'
UPDATE `sys_menu` SET `path` = 'budg/item' WHERE `menu_code` = 'BUDG_ITEM';

-- 预算流程配置：数据库是 'process'，前端路由是 '/hrp/budg/process'，应该改为 'budg/process'
UPDATE `sys_menu` SET `path` = 'budg/process' WHERE `menu_code` = 'BUDG_PROCESS';

-- 基础参数设置：数据库是 'param'，前端路由是 '/hrp/budg/param'，应该改为 'budg/param'
UPDATE `sys_menu` SET `path` = 'budg/param' WHERE `menu_code` = 'BUDG_PARAM';

-- 预算执行与控制：数据库是 'execution'，前端路由是 '/hrp/budg/execution'，应该改为 'budg/execution'
UPDATE `sys_menu` SET `path` = 'budg/execution' WHERE `menu_code` = 'BUDG_EXECUTION';

-- 预算调整与分析（二级菜单）：数据库是 'adjustment'，其下有具体功能菜单，路径建议改为 'budg/adjustment'
UPDATE `sys_menu` SET `path` = 'budg/adjustment' WHERE `menu_code` = 'BUDG_ADJUSTMENT';

-- 预算调整管理：数据库是 'adjustment-manage'，前端路由是 '/hrp/budg/adjustment-manage'，应该改为 'budg/adjustment-manage'
UPDATE `sys_menu` SET `path` = 'budg/adjustment-manage' WHERE `menu_code` = 'BUDG_ADJUSTMENT_MANAGE';

-- 预算执行分析：数据库是 'analysis'，前端路由是 '/hrp/budg/analysis'，应该改为 'budg/analysis'
UPDATE `sys_menu` SET `path` = 'budg/analysis' WHERE `menu_code` = 'BUDG_ANALYSIS';

-- ==================== 固定资产模块 ====================
-- 资产调拨审批：数据库是 '/hrp/asset/approval/transfer'，应该改为 'asset/approval/transfer'
UPDATE `sys_menu` SET `path` = 'asset/approval/transfer' WHERE `menu_code` = 'ASSET_APPROVAL_TRANSFER';

-- 资产处置审批：数据库是 '/hrp/asset/approval/disposal'，应该改为 'asset/approval/disposal'
UPDATE `sys_menu` SET `path` = 'asset/approval/disposal' WHERE `menu_code` = 'ASSET_APPROVAL_DISPOSAL';

-- 盘点差异审批：数据库是 '/hrp/asset/approval/inventory'，应该改为 'asset/approval/inventory'
UPDATE `sys_menu` SET `path` = 'asset/approval/inventory' WHERE `menu_code` = 'ASSET_APPROVAL_INVENTORY';

-- 资产变动审批：数据库是 '/hrp/asset/approval/change'，应该改为 'asset/approval/change'
UPDATE `sys_menu` SET `path` = 'asset/approval/change' WHERE `menu_code` = 'ASSET_APPROVAL_CHANGE';

-- 需求发起：数据库是 '/hrp/asset/procurement/apply'，应该改为 'asset/procurement/apply'
UPDATE `sys_menu` SET `path` = 'asset/procurement/apply' WHERE `menu_code` = 'ASSET_PROCUREMENT_APPLY';

-- 需求审核：数据库是 '/hrp/asset/procurement/approval'，应该改为 'asset/procurement/approval'
UPDATE `sys_menu` SET `path` = 'asset/procurement/approval' WHERE `menu_code` = 'ASSET_PROCUREMENT_APPROVAL';

-- 资产查询：数据库是 '/hrp/asset/query/asset'，应该改为 'asset/query/asset'
UPDATE `sys_menu` SET `path` = 'asset/query/asset' WHERE `menu_code` = 'ASSET_QUERY_ASSET';

-- 采购查询：数据库是 '/hrp/asset/query/procurement'，应该改为 'asset/query/procurement'
UPDATE `sys_menu` SET `path` = 'asset/query/procurement' WHERE `menu_code` = 'ASSET_QUERY_PROCUREMENT';

-- ==================== 验证查询 ====================
-- 执行完 UPDATE 后，可以运行以下查询验证路径是否正确：
-- SELECT menu_code, menu_name, path FROM sys_menu WHERE path LIKE '/hrp%' AND menu_type = 2 ORDER BY menu_code;
-- 应该没有结果（除了可能的一级菜单），因为所有三级菜单路径都应该是相对路径（不以 /hrp 开头）
