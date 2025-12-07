package com.hrp.user.mapper;

import com.hrp.common.entity.Employee;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 职工数据访问接口
 */
public interface EmployeeMapper {
    /**
     * 根据ID查询职工
     */
    Employee selectById(@Param("empId") Long empId);

    /**
     * 根据编码查询职工
     */
    Employee selectByCode(@Param("empCode") String empCode);

    /**
     * 查询所有职工
     */
    List<Employee> selectAll();

    /**
     * 根据部门ID查询职工列表
     */
    List<Employee> selectByDeptId(@Param("deptId") Long deptId);

    /**
     * 插入职工
     */
    int insert(Employee employee);

    /**
     * 更新职工
     */
    int updateById(Employee employee);

    /**
     * 删除职工
     */
    int deleteById(@Param("empId") Long empId);
}
