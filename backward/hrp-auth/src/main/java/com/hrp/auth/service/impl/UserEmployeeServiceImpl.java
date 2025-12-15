package com.hrp.auth.service.impl;

import com.hrp.common.entity.Employee;
import com.hrp.common.entity.PageResult;
import com.hrp.common.entity.UserWithEmployee;
import com.hrp.auth.mapper.UserEmployeeMapper;
import com.hrp.auth.service.UserEmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户和员工关联服务实现类
 */
@Service
public class UserEmployeeServiceImpl implements UserEmployeeService {

    @Autowired
    private UserEmployeeMapper userEmployeeMapper;

    @Override
    public List<UserWithEmployee> getAllEmployeesWithUser() {
        return userEmployeeMapper.selectAllEmployeesWithUser();
    }

    @Override
    public List<UserWithEmployee> getEmployeesByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllEmployeesWithUser();
        }
        return userEmployeeMapper.selectEmployeesByKeyword(keyword.trim());
    }

    @Override
    public PageResult<UserWithEmployee> getAllEmployeesWithUserPage(Long page, Long size) {
        if (page == null || page < 1) {
            page = 1L;
        }
        if (size == null || size < 1) {
            size = 10L;
        }
        Long offset = (page - 1) * size;
        List<UserWithEmployee> records = userEmployeeMapper.selectAllEmployeesWithUserPage(offset, size);
        Long total = userEmployeeMapper.countAllEmployeesWithUser();
        return PageResult.of(records, total, size, page);
    }

    @Override
    public PageResult<UserWithEmployee> getEmployeesByKeywordPage(String keyword, Long page, Long size) {
        if (page == null || page < 1) {
            page = 1L;
        }
        if (size == null || size < 1) {
            size = 10L;
        }
        Long offset = (page - 1) * size;
        String searchKeyword = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();
        List<UserWithEmployee> records;
        Long total;
        if (searchKeyword == null) {
            records = userEmployeeMapper.selectAllEmployeesWithUserPage(offset, size);
            total = userEmployeeMapper.countAllEmployeesWithUser();
        } else {
            records = userEmployeeMapper.selectEmployeesByKeywordPage(searchKeyword, offset, size);
            total = userEmployeeMapper.countEmployeesByKeyword(searchKeyword);
        }
        return PageResult.of(records, total, size, page);
    }

    @Override
    public Employee getEmployeeByCode(String empCode) {
        return userEmployeeMapper.selectEmployeeByCode(empCode);
    }

    @Override
    public List<Employee> getAllEmployees() {
        return userEmployeeMapper.selectAllEmployees();
    }

    @Override
    public boolean saveEmployee(Employee employee) {
        return userEmployeeMapper.insertEmployee(employee) > 0;
    }

    @Override
    public boolean updateEmployee(Employee employee) {
        return userEmployeeMapper.updateEmployee(employee) > 0;
    }

    @Override
    public boolean deleteEmployeeByCode(String empCode) {
        return userEmployeeMapper.deleteEmployeeByCode(empCode) > 0;
    }
}

