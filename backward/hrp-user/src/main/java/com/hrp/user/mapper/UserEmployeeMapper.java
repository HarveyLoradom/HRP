package com.hrp.user.mapper;

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
}

