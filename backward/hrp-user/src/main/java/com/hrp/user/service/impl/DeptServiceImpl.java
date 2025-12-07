package com.hrp.user.service.impl;

import com.hrp.user.mapper.DeptMapper;
import com.hrp.user.service.DeptService;
import com.hrp.common.entity.Dept;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门服务实现类
 */
@Service
public class DeptServiceImpl implements DeptService {

    @Autowired
    private DeptMapper deptMapper;

    @Override
    public Dept getById(Long deptId) {
        return deptMapper.selectById(deptId);
    }

    @Override
    public Dept getByCode(String deptCode) {
        return deptMapper.selectByCode(deptCode);
    }

    @Override
    public List<Dept> getAll() {
        return deptMapper.selectAll();
    }

    @Override
    public List<Dept> getBySuperDeptCode(String superDeptCode) {
        return deptMapper.selectBySuperDeptCode(superDeptCode);
    }

    @Override
    @Transactional
    public boolean save(Dept dept) {
        if (dept.getIsStop() == null) {
            dept.setIsStop(0L);
        }
        if (dept.getCreateTime() == null) {
            dept.setCreateTime(LocalDateTime.now());
        }
        return deptMapper.insert(dept) > 0;
    }

    @Override
    @Transactional
    public boolean update(Dept dept) {
        dept.setUpdateTime(LocalDateTime.now());
        return deptMapper.updateById(dept) > 0;
    }

    @Override
    @Transactional
    public boolean delete(Long deptId) {
        Dept dept = new Dept();
        dept.setDeptId(deptId);
        dept.setIsStop(1L);
        return deptMapper.updateById(dept) > 0;
    }
}
