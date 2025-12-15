package com.hrp.auth.mapper;

import com.hrp.common.entity.Employee;
import com.hrp.common.entity.UserWithEmployee;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户和员工关联查询Mapper
 */
public interface UserEmployeeMapper {
    /**
     * 查询所有员工（关联用户信息）
     */
    List<UserWithEmployee> selectAllEmployeesWithUser();

    /**
     * 根据工号或姓名查询员工（关联用户信息）
     */
    List<UserWithEmployee> selectEmployeesByKeyword(@Param("keyword") String keyword);

    /**
     * 分页查询所有员工（关联用户信息）
     */
    List<UserWithEmployee> selectAllEmployeesWithUserPage(@Param("offset") Long offset, @Param("size") Long size);

    /**
     * 分页查询员工（关联用户信息）- 带关键词
     */
    List<UserWithEmployee> selectEmployeesByKeywordPage(@Param("keyword") String keyword, @Param("offset") Long offset, @Param("size") Long size);

    /**
     * 统计所有员工数量
     */
    Long countAllEmployeesWithUser();

    /**
     * 统计带关键词的员工数量
     */
    Long countEmployeesByKeyword(@Param("keyword") String keyword);

    /**
     * 根据工号查询员工信息（只查询sys_emp表）
     */
    Employee selectEmployeeByCode(@Param("empCode") String empCode);

    /**
     * 查询所有员工列表（只查询sys_emp表，供其他功能使用）
     */
    List<Employee> selectAllEmployees();

    /**
     * 插入员工记录
     */
    int insertEmployee(Employee employee);

    /**
     * 更新员工记录
     */
    int updateEmployee(Employee employee);

    /**
     * 根据工号删除员工记录
     */
    int deleteEmployeeByCode(@Param("empCode") String empCode);
}

