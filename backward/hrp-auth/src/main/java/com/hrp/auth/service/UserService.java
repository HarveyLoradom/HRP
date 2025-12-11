package com.hrp.auth.service;

import com.hrp.common.entity.User;
import com.hrp.common.entity.UserLogin;

/**
 * 用户服务接口
 */
public interface UserService {
    /**
     * 根据账号查询用户
     */
    User getByAccount(String account);

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
     * 解锁用户并重置失败次数
     */
    void unlockUser(String userId);

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
}

