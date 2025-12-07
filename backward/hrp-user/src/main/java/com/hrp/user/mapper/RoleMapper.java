package com.hrp.user.mapper;

import com.hrp.common.entity.Role;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色数据访问接口
 */
public interface RoleMapper {
    /**
     * 根据ID查询角色
     */
    Role selectById(@Param("roleId") Long roleId);

    /**
     * 根据编码查询角色
     */
    Role selectByCode(@Param("roleCode") String roleCode);

    /**
     * 查询所有角色
     */
    List<Role> selectAll();

    /**
     * 插入角色
     */
    int insert(Role role);

    /**
     * 更新角色
     */
    int updateById(Role role);

    /**
     * 删除角色
     */
    int deleteById(@Param("roleId") Long roleId);
}
