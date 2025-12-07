package com.hrp.user.service.impl;

import com.hrp.user.mapper.RoleMapper;
import com.hrp.user.service.RoleService;
import com.hrp.common.entity.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色服务实现类
 */
@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleMapper roleMapper;

    @Override
    public Role getById(Long roleId) {
        return roleMapper.selectById(roleId);
    }

    @Override
    public Role getByCode(String roleCode) {
        return roleMapper.selectByCode(roleCode);
    }

    @Override
    public List<Role> getAll() {
        return roleMapper.selectAll();
    }

    @Override
    @Transactional
    public boolean save(Role role) {
        if (role.getIsStop() == null) {
            role.setIsStop(0L);
        }
        if (role.getCreateTime() == null) {
            role.setCreateTime(LocalDateTime.now());
        }
        return roleMapper.insert(role) > 0;
    }

    @Override
    @Transactional
    public boolean update(Role role) {
        role.setUpdateTime(LocalDateTime.now());
        return roleMapper.updateById(role) > 0;
    }

    @Override
    @Transactional
    public boolean delete(Long roleId) {
        Role role = new Role();
        role.setRoleId(roleId);
        role.setIsStop(1L);
        return roleMapper.updateById(role) > 0;
    }
}
