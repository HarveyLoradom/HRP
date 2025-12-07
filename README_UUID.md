# UUID用户ID说明

## 变更说明

系统已将 `sys_user` 表的 `id` 字段改为使用 **UUID** 作为主键，确保全局唯一性。

## 数据库表结构变更

### sys_user 表
- `id` 字段类型：`VARCHAR(36)` - 存储UUID（标准格式，带横线）
- 或使用 `VARCHAR(32)` - 存储UUID（无横线格式）

### 相关表
以下表的 `user_id` 字段已更新为 `VARCHAR(36)`：
- `sys_user_login` - 用户登录信息表
- `sys_user_menu` - 用户菜单关联表
- `sys_user_role` - 用户角色关联表（如果存在）

## 生成管理员用户

### 步骤1：运行密码生成工具

```bash
cd backward/hrp-auth
mvn compile exec:java -Dexec.mainClass="com.hrp.auth.util.PasswordGenerator"
```

工具会输出：
- 生成的UUID（32位，无横线）
- BCrypt加密的密码哈希值
- 完整的SQL插入语句

### 步骤2：执行SQL语句

将工具生成的SQL语句复制并执行：

```sql
USE hrp_db;

-- 插入管理员用户（替换为工具生成的UUID和密码哈希）
INSERT INTO `sys_user` (`id`, `account`, `name`, `type`, `password`, `is_stop`) VALUES
('生成的UUID', 'ADMIN001', '系统管理员', 1, '生成的BCrypt哈希值', 0);

-- 插入用户登录信息
INSERT INTO `sys_user_login` (`user_id`, `account`, `locked`, `login_fail_count`) VALUES
('生成的UUID', 'ADMIN001', 0, 0);

-- 为管理员分配所有菜单权限
INSERT INTO `sys_user_menu` (`user_id`, `menu_id`) 
SELECT '生成的UUID', id FROM sys_menu WHERE status = 1;
```

## UUID格式

系统支持两种UUID格式：
1. **标准格式**（36位，带横线）：`550e8400-e29b-41d4-a716-446655440000`
2. **紧凑格式**（32位，无横线）：`550e8400e29b41d4a716446655440000`

**推荐使用紧凑格式（32位）**，因为：
- 更节省存储空间
- 在URL和API中更友好
- 数据库字段可以设置为 `VARCHAR(32)`

## 代码中使用UUID

### 生成UUID

```java
import com.hrp.common.util.UuidUtil;

// 生成32位UUID（推荐）
String userId = UuidUtil.generateUuid();

// 生成36位标准UUID
String userId = UuidUtil.generateStandardUuid();
```

### 创建用户时自动生成UUID

在创建用户的Service方法中：

```java
User user = new User();
user.setId(UuidUtil.generateUuid()); // 自动生成UUID
user.setAccount("USER001");
// ... 其他字段
userMapper.insert(user);
```

## 注意事项

1. **UUID唯一性**：每次运行 `PasswordGenerator` 都会生成不同的UUID，这是正常的
2. **向后兼容**：如果数据库中已有使用固定ID（如'ADMIN001'）的用户，需要迁移到UUID
3. **关联表更新**：所有引用 `user_id` 的表都需要使用相同的UUID值
4. **登录账号**：登录时仍然使用 `account` 字段（工号），不是UUID

## 迁移现有数据

如果数据库中有使用固定ID的用户，需要迁移：

```sql
-- 1. 为现有用户生成新的UUID
UPDATE sys_user SET id = UUID() WHERE id = 'ADMIN001';

-- 2. 更新关联表（注意：需要先获取新生成的UUID）
-- 假设新UUID是 'new-uuid-here'
UPDATE sys_user_login SET user_id = 'new-uuid-here' WHERE user_id = 'ADMIN001';
UPDATE sys_user_menu SET user_id = 'new-uuid-here' WHERE user_id = 'ADMIN001';
```

## 验证

执行以下SQL验证用户数据：

```sql
SELECT 
    u.id,
    u.account,
    u.name,
    LENGTH(u.id) as uuid_length,
    ul.locked,
    ul.login_fail_count,
    COUNT(um.menu_id) as menu_count
FROM sys_user u
LEFT JOIN sys_user_login ul ON u.id = ul.user_id
LEFT JOIN sys_user_menu um ON u.id = um.user_id
WHERE u.account = 'ADMIN001'
GROUP BY u.id, u.account, u.name, ul.locked, ul.login_fail_count;
```

