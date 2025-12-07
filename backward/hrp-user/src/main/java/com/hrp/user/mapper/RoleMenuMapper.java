package com.hrp.user.mapper;

import com.hrp.common.entity.RoleMenu;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色菜单关联数据访问接口
 */
public interface RoleMenuMapper {
    /**
     * 根据角色ID查询菜单列表
     */
    List<RoleMenu> selectByRoleId(@Param("roleId") Long roleId);

    /**
     * 插入角色菜单关联
     */
    int insert(RoleMenu roleMenu);

    /**
     * 删除角色菜单关联
     */
    int deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 删除角色菜单关联
     */
    int deleteByRoleIdAndMenuId(@Param("roleId") Long roleId, @Param("menuId") Long menuId);
}
