package com.hrp.user.service;

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
}

