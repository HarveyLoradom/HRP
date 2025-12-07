package com.hrp.user.service.impl;

import com.hrp.user.mapper.RoleMenuMapper;
import com.hrp.user.service.RoleMenuService;
import com.hrp.common.entity.RoleMenu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色菜单关联服务实现类
 */
@Service
public class RoleMenuServiceImpl implements RoleMenuService {

    @Autowired
    private RoleMenuMapper roleMenuMapper;

    @Override
    public List<RoleMenu> getByRoleId(Long roleId) {
        return roleMenuMapper.selectByRoleId(roleId);
    }

    @Override
    @Transactional
    public boolean assignMenus(Long roleId, List<Long> menuIds) {
        // 先清空角色现有菜单
        roleMenuMapper.deleteByRoleId(roleId);
        
        // 分配新菜单
        if (menuIds != null && !menuIds.isEmpty()) {
            for (Long menuId : menuIds) {
                RoleMenu roleMenu = new RoleMenu();
                roleMenu.setRoleId(roleId);
                roleMenu.setMenuId(menuId);
                roleMenu.setCreateTime(LocalDateTime.now());
                roleMenuMapper.insert(roleMenu);
            }
        }
        return true;
    }

    @Override
    @Transactional
    public boolean removeMenu(Long roleId, Long menuId) {
        return roleMenuMapper.deleteByRoleIdAndMenuId(roleId, menuId) > 0;
    }

    @Override
    @Transactional
    public boolean clearMenus(Long roleId) {
        return roleMenuMapper.deleteByRoleId(roleId) >= 0;
    }
}
