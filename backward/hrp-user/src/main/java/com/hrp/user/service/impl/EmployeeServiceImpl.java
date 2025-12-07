package com.hrp.user.service.impl;

import com.hrp.user.mapper.EmployeeMapper;
import com.hrp.user.service.EmployeeService;
import com.hrp.common.entity.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 职工服务实现类
 */
@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    @Override
    public Employee getById(Long empId) {
        return employeeMapper.selectById(empId);
    }

    @Override
    public Employee getByCode(String empCode) {
        return employeeMapper.selectByCode(empCode);
    }

    @Override
    public List<Employee> getAll() {
        return employeeMapper.selectAll();
    }

    @Override
    public List<Employee> getByDeptId(Long deptId) {
        return employeeMapper.selectByDeptId(deptId);
    }

    @Override
    @Transactional
    public boolean save(Employee employee) {
        if (employee.getIsStop() == null) {
            employee.setIsStop(0L);
        }
        if (employee.getCreateTime() == null) {
            employee.setCreateTime(LocalDateTime.now());
        }
        return employeeMapper.insert(employee) > 0;
    }

    @Override
    @Transactional
    public boolean update(Employee employee) {
        employee.setUpdateTime(LocalDateTime.now());
        return employeeMapper.updateById(employee) > 0;
    }

    @Override
    @Transactional
    public boolean delete(Long empId) {
        Employee employee = new Employee();
        employee.setEmpId(empId);
        employee.setIsStop(1L);
        return employeeMapper.updateById(employee) > 0;
    }
}
