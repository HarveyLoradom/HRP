package com.hrp.user.service;

import com.hrp.common.entity.User;

import java.util.List;

/**
 * 用户管理服务接口
 */
public interface UserService {
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
     * 删除用户（逻辑删除）
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
}



