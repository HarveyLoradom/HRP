package com.hrp.auth.service;

import com.hrp.common.entity.UserTypeMenu;

import java.util.List;

/**
 * 用户类型菜单关联服务接口
 */
public interface UserTypeMenuService {
    /**
     * 根据用户类型查询菜单列表
     */
    List<UserTypeMenu> getByUserType(String userType);

    /**
     * 分配用户类型菜单
     */
    boolean assignMenus(String userType, List<Long> menuIds);

    /**
     * 移除用户类型菜单
     */
    boolean removeMenu(String userType, Long menuId);

    /**
     * 清空用户类型所有菜单
     */
    boolean clearMenus(String userType);
}

