package com.hrp.auth.service;

import com.hrp.common.entity.User;
import com.hrp.common.entity.UserLogin;

import java.util.List;

/**
 * 用户服务接口（包含用户管理和登录相关功能）
 */
public interface UserService {
    // ========== 用户管理相关 ==========
    /**
     * 根据ID查询用户
     */
    User getById(String id);

    /**
     * 根据账号查询用户
     */
    User getByAccount(String account);

    /**
     * 查询所有用户
     */
    List<User> getAll();

    /**
     * 新增用户
     */
    boolean save(User user);

    /**
     * 更新用户
     */
    boolean update(User user);

    /**
     * 删除用户（物理删除，同时删除sys_user、sys_emp、sys_user_login及相关关联数据）
     */
    boolean delete(String id);

    /**
     * 启用/停用用户
     */
    boolean toggleStatus(String id);

    /**
     * 重置用户密码（使用系统配置的原始密码）
     */
    boolean resetPassword(String id);

    /**
     * 解锁用户（解除账户锁定）
     */
    boolean unlockUser(String id);

    // ========== 登录相关 ==========
    /**
     * 获取或创建用户登录信息
     */
    UserLogin getOrCreateUserLogin(String userId, String account);

    /**
     * 更新登录失败次数
     */
    void updateLoginFailCount(String userId, Integer count);

    /**
     * 锁定用户
     */
    void lockUser(String userId);

    /**
     * 更新最后登录时间
     */
    void updateLastLoginTime(String userId);

    /**
     * 获取用户登录信息
     */
    UserLogin getUserLogin(String userId);

    /**
     * 验证密码
     */
    boolean validatePassword(String rawPassword, String encodedPassword);

    /**
     * 修改密码
     */
    boolean changePassword(String userId, String oldPassword, String newPassword);

    /**
     * 强制修改密码（不需要原密码）
     */
    boolean forceChangePassword(String userId, String newPassword);

    /**
     * 批量导入用户
     */
    com.hrp.common.entity.Result<String> importUsers(java.util.List<java.util.List<String>> dataList, String createUser);
}

