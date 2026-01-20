package com.hrp.auth.service.impl;

import com.hrp.common.entity.Employee;
import com.hrp.common.entity.PageResult;
import com.hrp.common.entity.UserWithEmployee;
import com.hrp.auth.mapper.UserEmployeeMapper;
import com.hrp.auth.service.UserEmployeeService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
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
    public List<UserWithEmployee> getAllEmployeesWithUser(Long isStop) {
        return userEmployeeMapper.selectAllEmployeesWithUser(isStop);
    }

    @Override
    public List<UserWithEmployee> getEmployeesByKeyword(String keyword, Long isStop) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllEmployeesWithUser(isStop);
        }
        return userEmployeeMapper.selectEmployeesByKeyword(keyword.trim(), isStop);
    }

    @Override
    public PageResult<UserWithEmployee> getAllEmployeesWithUserPage(Long isStop, Long page, Long size) {
        if (page == null || page < 1) {
            page = 1L;
        }
        if (size == null || size < 1) {
            size = 10L;
        }
        PageHelper.startPage(page.intValue(), size.intValue());
        List<UserWithEmployee> list = userEmployeeMapper.selectAllEmployeesWithUser(isStop);
        PageInfo<UserWithEmployee> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), size, page);
    }

    @Override
    public PageResult<UserWithEmployee> getEmployeesByKeywordPage(String keyword, Long isStop, Long page, Long size) {
        if (page == null || page < 1) {
            page = 1L;
        }
        if (size == null || size < 1) {
            size = 10L;
        }
        String searchKeyword = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();
        PageHelper.startPage(page.intValue(), size.intValue());
        List<UserWithEmployee> list;
        if (searchKeyword == null) {
            list = userEmployeeMapper.selectAllEmployeesWithUser(isStop);
        } else {
            list = userEmployeeMapper.selectEmployeesByKeyword(searchKeyword, isStop);
        }
        PageInfo<UserWithEmployee> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), size, page);
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

