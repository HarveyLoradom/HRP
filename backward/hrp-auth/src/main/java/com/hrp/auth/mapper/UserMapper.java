package com.hrp.auth.mapper;

import com.hrp.common.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户数据访问接口
 */
public interface UserMapper {
    /**
     * 根据ID查询用户
     */
    User selectById(@Param("id") String id);

    /**
     * 根据账号查询用户
     */
    User selectByAccount(@Param("account") String account);

    /**
     * 插入用户
     */
    int insert(User user);

    /**
     * 更新用户
     */
    int updateById(User user);

    /**
     * 删除用户
     */
    int deleteById(@Param("id") String id);

    /**
     * 查询所有用户
     */
    List<User> selectAll();
}

