package com.hrp.auth.service.impl;

import com.hrp.auth.mapper.UserMenuMapper;
import com.hrp.auth.service.UserMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户菜单关联服务实现类
 */
@Service
public class UserMenuServiceImpl implements UserMenuService {

    @Autowired
    private UserMenuMapper userMenuMapper;

    @Override
    public List<Long> getMenuIdsByUserId(String userId) {
        return userMenuMapper.selectMenuIdsByUserId(userId);
    }

    @Override
    @Transactional
    public boolean saveUserMenus(String userId, List<Long> menuIds) {
        // 先删除用户的所有菜单权限
        userMenuMapper.deleteByUserId(userId);
        
        // 如果有新的菜单权限，则批量插入
        if (menuIds != null && !menuIds.isEmpty()) {
            int result = userMenuMapper.batchInsert(userId, menuIds);
            return result > 0;
        }
        
        return true; // 如果没有菜单权限，删除成功即可
    }
}

