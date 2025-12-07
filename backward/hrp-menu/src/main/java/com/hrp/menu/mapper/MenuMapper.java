package com.hrp.menu.mapper;

import com.hrp.common.entity.Menu;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 菜单数据访问接口
 */
public interface MenuMapper {
    /**
     * 根据ID查询菜单
     */
    Menu selectById(@Param("id") Long id);

    /**
     * 查询所有菜单
     */
    List<Menu> selectAll();

    /**
     * 根据用户ID查询菜单列表
     */
    List<Menu> selectMenusByUserId(@Param("userId") String userId);

    /**
     * 插入菜单
     */
    int insert(Menu menu);

    /**
     * 更新菜单
     */
    int updateById(Menu menu);

    /**
     * 删除菜单
     */
    int deleteById(@Param("id") Long id);
}

