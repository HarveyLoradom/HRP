package com.hrp.auth.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户菜单关联数据访问接口
 */
public interface UserMenuMapper {
    /**
     * 根据用户ID查询菜单ID列表
     */
    List<Long> selectMenuIdsByUserId(@Param("userId") String userId);

    /**
     * 根据用户ID删除所有菜单关联
     */
    int deleteByUserId(@Param("userId") String userId);

    /**
     * 批量插入用户菜单关联
     */
    int batchInsert(@Param("userId") String userId, @Param("menuIds") List<Long> menuIds);
}

