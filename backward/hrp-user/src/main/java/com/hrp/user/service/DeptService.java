package com.hrp.user.service;

import com.hrp.common.entity.Dept;

import java.util.List;

/**
 * 部门服务接口
 */
public interface DeptService {
    /**
     * 根据ID查询部门
     */
    Dept getById(Long deptId);

    /**
     * 根据编码查询部门
     */
    Dept getByCode(String deptCode);

    /**
     * 查询所有部门
     */
    List<Dept> getAll();

    /**
     * 根据上级部门编码查询部门列表
     */
    List<Dept> getBySuperDeptCode(String superDeptCode);

    /**
     * 新增部门
     */
    boolean save(Dept dept);

    /**
     * 更新部门
     */
    boolean update(Dept dept);

    /**
     * 删除部门（逻辑删除）
     */
    boolean delete(Long deptId);
}
