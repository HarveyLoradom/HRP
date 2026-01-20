package com.hrp.auth.mapper.impl;

import com.hrp.auth.mapper.UserTypeMenuMapper;
import com.hrp.common.entity.UserTypeMenu;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户类型菜单关联数据访问实现类
 */
@Repository
public class UserTypeMenuMapperImpl implements UserTypeMenuMapper {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    private static final String NAMESPACE = "com.hrp.auth.mapper.UserTypeMenuMapper";

    @Override
    public List<UserTypeMenu> selectByUserType(String userType) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByUserType", userType);
    }

    @Override
    public int insert(UserTypeMenu userTypeMenu) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", userTypeMenu);
    }

    @Override
    public int deleteByUserType(String userType) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteByUserType", userType);
    }

    @Override
    public int deleteByUserTypeAndMenuId(String userType, Long menuId) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("userType", userType);
        params.put("menuId", menuId);
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteByUserTypeAndMenuId", params);
    }
}

