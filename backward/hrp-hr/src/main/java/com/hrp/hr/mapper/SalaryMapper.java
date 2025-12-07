package com.hrp.hr.mapper;

import com.hrp.common.entity.Salary;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SalaryMapper {
    Salary selectById(@Param("id") Long id);
    Salary selectByEmpIdAndMonth(@Param("empId") Long empId, @Param("salaryMonth") String salaryMonth);
    List<Salary> selectByEmpId(@Param("empId") Long empId);
    List<Salary> selectByMonth(@Param("salaryMonth") String salaryMonth);
    List<Salary> selectAll();
    int insert(Salary salary);
    int updateById(Salary salary);
    int deleteById(@Param("id") Long id);
}

