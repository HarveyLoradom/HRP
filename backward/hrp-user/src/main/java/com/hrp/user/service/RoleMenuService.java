package com.hrp.user.service;

import com.hrp.common.entity.RoleMenu;

import java.util.List;

/**
 * 角色菜单关联服务接口
 */
public interface RoleMenuService {
    /**
     * 根据角色ID查询菜单列表
     */
    List<RoleMenu> getByRoleId(Long roleId);

    /**
     * 分配角色菜单
     */
    boolean assignMenus(Long roleId, List<Long> menuIds);

    /**
     * 移除角色菜单
     */
    boolean removeMenu(Long roleId, Long menuId);

    /**
     * 清空角色所有菜单
     */
    boolean clearMenus(Long roleId);
}
