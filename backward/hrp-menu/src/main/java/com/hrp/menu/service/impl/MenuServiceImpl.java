package com.hrp.menu.service.impl;

import com.hrp.common.entity.Menu;
import com.hrp.menu.mapper.MenuMapper;
import com.hrp.menu.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuServiceImpl implements MenuService {

    @Autowired
    private MenuMapper menuMapper;

    /**
     * 获取所有菜单（树形结构）
     */
    @Override
    public List<Menu> getAllMenus() {
        List<Menu> allMenus = menuMapper.selectAll();
        // 过滤出启用的菜单并按排序字段排序
        List<Menu> enabledMenus = allMenus.stream()
                .filter(menu -> menu.getStatus() != null && menu.getStatus() == 1)
                .sorted((m1, m2) -> {
                    Integer sort1 = m1.getSort() != null ? m1.getSort() : 0;
                    Integer sort2 = m2.getSort() != null ? m2.getSort() : 0;
                    return sort1.compareTo(sort2);
                })
                .collect(Collectors.toList());
        return buildMenuTree(enabledMenus, 0L);
    }

    /**
     * 根据用户ID获取菜单列表（树形结构）
     */
    @Override
    public List<Menu> getMenusByUserId(String userId) {
        List<Menu> menus = menuMapper.selectMenusByUserId(userId);
        return buildMenuTree(menus, 0L);
    }

    /**
     * 构建菜单树
     */
    private List<Menu> buildMenuTree(List<Menu> menus, Long parentId) {
        List<Menu> tree = new ArrayList<>();
        for (Menu menu : menus) {
            if (parentId.equals(menu.getParentId())) {
                List<Menu> children = buildMenuTree(menus, menu.getId());
                menu.setChildren(children);
                tree.add(menu);
            }
        }
        return tree;
    }

    /**
     * 根据ID获取菜单
     */
    @Override
    public Menu getById(Long id) {
        return menuMapper.selectById(id);
    }

    /**
     * 新增菜单
     */
    @Override
    public boolean save(Menu menu) {
        return menuMapper.insert(menu) > 0;
    }

    /**
     * 更新菜单
     */
    @Override
    public boolean update(Menu menu) {
        return menuMapper.updateById(menu) > 0;
    }

    /**
     * 删除菜单
     */
    @Override
    public boolean delete(Long id) {
        return menuMapper.deleteById(id) > 0;
    }
}

