package com.hrp.auth.mapper;

import com.hrp.common.entity.UserTypeMenu;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户类型菜单关联数据访问接口
 */
public interface UserTypeMenuMapper {
    /**
     * 根据用户类型查询菜单列表
     */
    List<UserTypeMenu> selectByUserType(@Param("userType") String userType);

    /**
     * 插入用户类型菜单关联
     */
    int insert(UserTypeMenu userTypeMenu);

    /**
     * 删除用户类型菜单关联
     */
    int deleteByUserType(@Param("userType") String userType);

    /**
     * 删除用户类型菜单关联
     */
    int deleteByUserTypeAndMenuId(@Param("userType") String userType, @Param("menuId") Long menuId);
}

