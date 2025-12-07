-- 快速设置管理员用户的SQL脚本
-- 注意：请先运行 PasswordGenerator 工具生成UUID和密码哈希值
-- 运行命令：cd backward/hrp-auth && mvn compile exec:java -Dexec.mainClass="com.hrp.auth.util.PasswordGenerator"

USE hrp_db;

-- ============================================
-- 步骤1：删除旧的管理员用户（如果存在）
-- ============================================
DELETE FROM sys_user_menu WHERE user_id IN (SELECT id FROM sys_user WHERE account = 'ADMIN001');
DELETE FROM sys_user_login WHERE account = 'ADMIN001';
DELETE FROM sys_user WHERE account = 'ADMIN001';

-- ============================================
-- 步骤2：插入新的管理员用户
-- 请将下面的 'YOUR_UUID_HERE' 和 'YOUR_PASSWORD_HASH_HERE' 替换为 PasswordGenerator 工具生成的值
-- ============================================

-- 示例（请替换为实际生成的值）：
-- INSERT INTO `sys_user` (`id`, `account`, `name`, `type`, `password`, `is_stop`) VALUES
-- ('YOUR_UUID_HERE', 'ADMIN001', '系统管理员', 1, 'YOUR_PASSWORD_HASH_HERE', 0);

-- INSERT INTO `sys_user_login` (`user_id`, `account`, `locked`, `login_fail_count`) VALUES
-- ('YOUR_UUID_HERE', 'ADMIN001', 0, 0);

-- ============================================
-- 步骤3：为管理员分配所有菜单权限
-- ============================================
-- INSERT INTO `sys_user_menu` (`user_id`, `menu_id`) 
-- SELECT 'YOUR_UUID_HERE', id FROM sys_menu WHERE status = 1;

-- ============================================
-- 验证用户数据
-- ============================================
SELECT 
    u.id,
    u.account,
    u.name,
    LENGTH(u.id) as uuid_length,
    CASE 
        WHEN u.id LIKE '%-%-%-%-%' THEN '标准UUID格式（36位）'
        WHEN LENGTH(u.id) = 32 THEN '紧凑UUID格式（32位）'
        ELSE '其他格式'
    END as uuid_format,
    ul.locked,
    ul.login_fail_count,
    COUNT(DISTINCT um.menu_id) as menu_count
FROM sys_user u
LEFT JOIN sys_user_login ul ON u.id = ul.user_id
LEFT JOIN sys_user_menu um ON u.id = um.user_id
WHERE u.account = 'ADMIN001'
GROUP BY u.id, u.account, u.name, ul.locked, ul.login_fail_count;

