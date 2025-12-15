package com.hrp.auth.service.impl;

import com.hrp.auth.mapper.UserRoleMapper;
import com.hrp.auth.service.UserRoleService;
import com.hrp.common.entity.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户角色关联服务实现类
 */
@Service
public class UserRoleServiceImpl implements UserRoleService {

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Override
    public List<UserRole> getByUserId(String userId) {
        return userRoleMapper.selectByUserId(userId);
    }

    @Override
    @Transactional
    public boolean assignRoles(String userId, List<Long> roleIds) {
        // 先清空用户现有角色
        userRoleMapper.deleteByUserId(userId);
        
        // 分配新角色
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                UserRole userRole = new UserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                userRole.setCreateTime(LocalDateTime.now());
                userRoleMapper.insert(userRole);
            }
        }
        return true;
    }

    @Override
    @Transactional
    public boolean removeRole(String userId, Long roleId) {
        return userRoleMapper.deleteByUserIdAndRoleId(userId, roleId) > 0;
    }

    @Override
    @Transactional
    public boolean clearRoles(String userId) {
        return userRoleMapper.deleteByUserId(userId) >= 0;
    }

    @Override
    @Transactional
    public boolean batchAssignRoles(List<String> userIds, List<Long> roleIds) {
        if (userIds == null || userIds.isEmpty()) {
            return false;
        }
        
        // 为每个用户分配角色
        for (String userId : userIds) {
            // 先清空用户现有角色
            userRoleMapper.deleteByUserId(userId);
            
            // 分配新角色
            if (roleIds != null && !roleIds.isEmpty()) {
                for (Long roleId : roleIds) {
                    UserRole userRole = new UserRole();
                    userRole.setUserId(userId);
                    userRole.setRoleId(roleId);
                    userRole.setCreateTime(LocalDateTime.now());
                    userRoleMapper.insert(userRole);
                }
            }
        }
        return true;
    }
}

