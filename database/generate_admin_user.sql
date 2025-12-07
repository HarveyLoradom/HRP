-- 生成管理员用户的SQL脚本
-- 使用方法：
-- 1. 运行 PasswordGenerator 工具生成UUID和密码哈希
-- 2. 将生成的SQL语句复制到此处或直接执行

-- 运行命令生成UUID和密码：
-- cd backward/hrp-auth
-- mvn compile exec:java -Dexec.mainClass="com.hrp.auth.util.PasswordGenerator"

-- 示例SQL（请替换为实际生成的值）：
-- 
-- -- 插入管理员用户
-- INSERT INTO `sys_user` (`id`, `account`, `name`, `type`, `password`, `is_stop`) VALUES
-- ('生成的UUID', 'ADMIN001', '系统管理员', 1, '生成的BCrypt哈希值', 0);
-- 
-- -- 插入用户登录信息
-- INSERT INTO `sys_user_login` (`user_id`, `account`, `locked`, `login_fail_count`) VALUES
-- ('生成的UUID', 'ADMIN001', 0, 0);
-- 
-- -- 为管理员分配所有菜单权限
-- INSERT INTO `sys_user_menu` (`user_id`, `menu_id`) 
-- SELECT '生成的UUID', id FROM sys_menu WHERE status = 1;

