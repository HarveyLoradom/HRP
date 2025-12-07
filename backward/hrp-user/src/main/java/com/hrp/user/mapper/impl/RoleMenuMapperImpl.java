package com.hrp.user.mapper.impl;

import com.hrp.user.mapper.RoleMenuMapper;
import com.hrp.common.entity.RoleMenu;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 角色菜单关联数据访问实现类
 */
@Repository
public class RoleMenuMapperImpl implements RoleMenuMapper {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    private static final String NAMESPACE = "com.hrp.user.mapper.RoleMenuMapper";

    @Override
    public List<RoleMenu> selectByRoleId(Long roleId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByRoleId", roleId);
    }

    @Override
    public int insert(RoleMenu roleMenu) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", roleMenu);
    }

    @Override
    public int deleteByRoleId(Long roleId) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteByRoleId", roleId);
    }

    @Override
    public int deleteByRoleIdAndMenuId(Long roleId, Long menuId) {
        RoleMenu roleMenu = new RoleMenu();
        roleMenu.setRoleId(roleId);
        roleMenu.setMenuId(menuId);
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteByRoleIdAndMenuId", roleMenu);
    }
}
