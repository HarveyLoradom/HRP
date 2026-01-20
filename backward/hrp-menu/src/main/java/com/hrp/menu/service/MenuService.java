package com.hrp.menu.service;

import com.hrp.common.entity.Menu;

import java.util.List;

/**
 * 菜单服务接口
 */
public interface MenuService {
    /**
     * 获取所有菜单（树形结构）
     */
    List<Menu> getAllMenus();

    /**
     * 根据用户ID获取菜单列表（树形结构）
     */
    List<Menu> getMenusByUserId(String userId);

    /**
     * 根据ID获取菜单
     */
    Menu getById(Long id);

    /**
     * 新增菜单
     */
    boolean save(Menu menu);

    /**
     * 更新菜单
     */
    boolean update(Menu menu);

    /**
     * 删除菜单
     */
    boolean delete(Long id);
}

