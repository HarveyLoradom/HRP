package com.hrp.user.service;

import java.util.List;

/**
 * 用户菜单关联服务接口
 */
public interface UserMenuService {
    /**
     * 根据用户ID查询菜单ID列表
     */
    List<Long> getMenuIdsByUserId(String userId);

    /**
     * 保存用户菜单权限（先删除旧的，再插入新的）
     */
    boolean saveUserMenus(String userId, List<Long> menuIds);
}



