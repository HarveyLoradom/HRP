package com.hrp.hr.service;

import com.hrp.common.entity.Salary;

import java.util.List;

public interface SalaryService {
    List<Salary> getAll();
    List<Salary> getByEmpId(Long empId);
    List<Salary> getByMonth(String salaryMonth);
    Salary getById(Long id);
    Salary getByEmpIdAndMonth(Long empId, String salaryMonth);
    boolean save(Salary salary);
    boolean update(Salary salary);
    boolean delete(Long id);
    boolean calculateSalary(Long empId, String salaryMonth);
}

