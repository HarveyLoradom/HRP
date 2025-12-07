package com.hrp.user.service;

import com.hrp.common.entity.Employee;

import java.util.List;

/**
 * 职工服务接口
 */
public interface EmployeeService {
    /**
     * 根据ID查询职工
     */
    Employee getById(Long empId);

    /**
     * 根据编码查询职工
     */
    Employee getByCode(String empCode);

    /**
     * 查询所有职工
     */
    List<Employee> getAll();

    /**
     * 根据部门ID查询职工列表
     */
    List<Employee> getByDeptId(Long deptId);

    /**
     * 新增职工
     */
    boolean save(Employee employee);

    /**
     * 更新职工
     */
    boolean update(Employee employee);

    /**
     * 删除职工（逻辑删除）
     */
    boolean delete(Long empId);
}
