package com.hrp.auth.service;

import com.hrp.common.entity.UserRole;

import java.util.List;

/**
 * 用户角色关联服务接口
 */
public interface UserRoleService {
    /**
     * 根据用户ID查询角色列表
     */
    List<UserRole> getByUserId(String userId);

    /**
     * 分配用户角色
     */
    boolean assignRoles(String userId, List<Long> roleIds);

    /**
     * 移除用户角色
     */
    boolean removeRole(String userId, Long roleId);

    /**
     * 清空用户所有角色
     */
    boolean clearRoles(String userId);

    /**
     * 批量分配用户角色
     */
    boolean batchAssignRoles(List<String> userIds, List<Long> roleIds);
}

