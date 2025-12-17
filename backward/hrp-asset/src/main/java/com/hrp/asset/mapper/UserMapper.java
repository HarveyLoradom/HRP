package com.hrp.asset.mapper;

import com.hrp.common.entity.User;
import org.apache.ibatis.annotations.Param;

/**
 * 用户数据访问接口（本地，直接访问数据库）
 */
public interface UserMapper {
    /**
     * 根据ID查询用户
     */
    User selectById(@Param("id") String id);
}












