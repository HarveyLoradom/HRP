package com.hrp.user.mapper;

import com.hrp.common.entity.UserRole;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户角色关联数据访问接口
 */
public interface UserRoleMapper {
    /**
     * 根据用户ID查询角色列表
     */
    List<UserRole> selectByUserId(@Param("userId") String userId);

    /**
     * 根据角色ID查询用户列表
     */
    List<UserRole> selectByRoleId(@Param("roleId") Long roleId);

    /**
     * 插入用户角色关联
     */
    int insert(UserRole userRole);

    /**
     * 删除用户角色关联
     */
    int deleteByUserId(@Param("userId") String userId);

    /**
     * 删除用户角色关联
     */
    int deleteByUserIdAndRoleId(@Param("userId") String userId, @Param("roleId") Long roleId);
}
