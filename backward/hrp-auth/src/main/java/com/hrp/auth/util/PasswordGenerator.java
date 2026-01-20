package com.hrp.auth.util;

import com.hrp.common.util.PasswordUtil;
import java.util.UUID;

/**
 * 密码生成工具
 * 用于生成加密后的密码和UUID，方便数据库初始化
 */
public class PasswordGenerator {
    public static void main(String[] args) {
        String password = "123456";
        
        // 生成UUID
        String userId = UUID.randomUUID().toString().replace("-", "");
        
        // 生成BCrypt加密的密码
        String encodedPassword = PasswordUtil.encode(password);
        
        System.out.println("=== 用户信息 ===");
        System.out.println("用户ID (UUID): " + userId);
        System.out.println("账号: admin");
        System.out.println("原始密码: " + password);
        System.out.println("加密后密码: " + encodedPassword);
        
        // 验证密码
        boolean matches = PasswordUtil.matches(password, encodedPassword);
        System.out.println("密码验证结果: " + matches);
        
        // 生成SQL语句
        System.out.println("\n=== SQL插入语句 ===");
        System.out.println("INSERT INTO `sys_user` (`id`, `account`, `name`, `type`, `password`, `is_stop`) VALUES");
        System.out.println("('" + userId + "', 'admin', '系统管理员', 1, '" + encodedPassword + "', 0);");
        System.out.println("\nINSERT INTO `sys_user_login` (`user_id`, `account`, `locked`, `login_fail_count`) VALUES");
        System.out.println("('" + userId + "', 'ADMIN001', 0, 0);");
    }
}
