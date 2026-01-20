package com.hrp.auth.mapper;

import com.hrp.common.entity.UserLogin;
import org.apache.ibatis.annotations.Param;

/**
 * 用户登录信息数据访问接口
 */
public interface UserLoginMapper {
    /**
     * 根据用户ID查询
     */
    UserLogin selectByUserId(@Param("userId") String userId);

    /**
     * 插入用户登录信息
     */
    int insert(UserLogin userLogin);

    /**
     * 更新用户登录信息
     */
    int updateById(UserLogin userLogin);

    /**
     * 根据用户ID删除用户登录信息
     */
    int deleteByUserId(@Param("userId") String userId);
}

