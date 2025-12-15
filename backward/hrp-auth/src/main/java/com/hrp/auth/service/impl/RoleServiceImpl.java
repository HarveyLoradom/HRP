package com.hrp.auth.service.impl;

import com.hrp.auth.mapper.RoleMapper;
import com.hrp.auth.service.RoleService;
import com.hrp.common.entity.Role;
import com.hrp.common.entity.PageResult;
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

    @Override
    public PageResult<Role> getAllPage(Long page, Long size) {
        if (page == null || page < 1) {
            page = 1L;
        }
        if (size == null || size < 1) {
            size = 10L;
        }
        Long offset = (page - 1) * size;
        List<Role> records = roleMapper.selectAllPage(offset, size);
        Long total = roleMapper.countAll();
        return PageResult.of(records, total, size, page);
    }
}

