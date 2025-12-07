package com.hrp.user.service;

import com.hrp.common.entity.Role;

import java.util.List;

/**
 * 角色服务接口
 */
public interface RoleService {
    /**
     * 根据ID查询角色
     */
    Role getById(Long roleId);

    /**
     * 根据编码查询角色
     */
    Role getByCode(String roleCode);

    /**
     * 查询所有角色
     */
    List<Role> getAll();

    /**
     * 新增角色
     */
    boolean save(Role role);

    /**
     * 更新角色
     */
    boolean update(Role role);

    /**
     * 删除角色（逻辑删除）
     */
    boolean delete(Long roleId);
}
