package com.hrp.auth.service;

import com.hrp.common.entity.Employee;
import com.hrp.common.entity.PageResult;
import com.hrp.common.entity.UserWithEmployee;

import java.util.List;

/**
 * 用户和员工关联服务接口
 */
public interface UserEmployeeService {
    /**
     * 查询所有员工（关联用户信息）
     */
    List<UserWithEmployee> getAllEmployeesWithUser();

    /**
     * 根据工号或姓名查询员工（关联用户信息）
     */
    List<UserWithEmployee> getEmployeesByKeyword(String keyword);

    /**
     * 分页查询所有员工（关联用户信息）
     */
    PageResult<UserWithEmployee> getAllEmployeesWithUserPage(Long page, Long size);

    /**
     * 分页查询员工（关联用户信息）- 带关键词
     */
    PageResult<UserWithEmployee> getEmployeesByKeywordPage(String keyword, Long page, Long size);

    /**
     * 根据工号查询员工信息（只查询sys_emp表）
     */
    Employee getEmployeeByCode(String empCode);

    /**
     * 查询所有员工列表（只查询sys_emp表，供其他功能使用）
     */
    List<Employee> getAllEmployees();

    /**
     * 插入员工记录
     */
    boolean saveEmployee(Employee employee);

    /**
     * 更新员工记录
     */
    boolean updateEmployee(Employee employee);

    /**
     * 根据工号删除员工记录
     */
    boolean deleteEmployeeByCode(String empCode);
}

