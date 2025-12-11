package com.hrp.user.service.impl;

import com.hrp.common.entity.UserWithEmployee;
import com.hrp.user.mapper.UserEmployeeMapper;
import com.hrp.user.service.UserEmployeeService;
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
}

