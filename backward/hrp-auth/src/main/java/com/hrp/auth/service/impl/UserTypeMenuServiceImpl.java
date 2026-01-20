package com.hrp.auth.service.impl;

import com.hrp.auth.mapper.UserTypeMenuMapper;
import com.hrp.auth.service.UserTypeMenuService;
import com.hrp.common.entity.UserTypeMenu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户类型菜单关联服务实现类
 */
@Service
public class UserTypeMenuServiceImpl implements UserTypeMenuService {

    @Autowired
    private UserTypeMenuMapper userTypeMenuMapper;

    @Override
    public List<UserTypeMenu> getByUserType(String userType) {
        return userTypeMenuMapper.selectByUserType(userType);
    }

    @Override
    @Transactional
    public boolean assignMenus(String userType, List<Long> menuIds) {
        // 先清空用户类型现有菜单
        userTypeMenuMapper.deleteByUserType(userType);
        
        // 分配新菜单
        if (menuIds != null && !menuIds.isEmpty()) {
            for (Long menuId : menuIds) {
                UserTypeMenu userTypeMenu = new UserTypeMenu();
                userTypeMenu.setUserType(userType);
                userTypeMenu.setMenuId(menuId);
                userTypeMenu.setCreateTime(LocalDateTime.now());
                userTypeMenuMapper.insert(userTypeMenu);
            }
        }
        return true;
    }

    @Override
    @Transactional
    public boolean removeMenu(String userType, Long menuId) {
        return userTypeMenuMapper.deleteByUserTypeAndMenuId(userType, menuId) > 0;
    }

    @Override
    @Transactional
    public boolean clearMenus(String userType) {
        return userTypeMenuMapper.deleteByUserType(userType) >= 0;
    }
}

