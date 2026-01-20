package com.hrp.auth.mapper.impl;

import com.hrp.auth.mapper.UserRoleMapper;
import com.hrp.common.entity.UserRole;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户角色关联数据访问实现类
 */
@Repository
public class UserRoleMapperImpl implements UserRoleMapper {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    private static final String NAMESPACE = "com.hrp.auth.mapper.UserRoleMapper";

    @Override
    public List<UserRole> selectByUserId(String userId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByUserId", userId);
    }

    @Override
    public List<UserRole> selectByRoleId(Long roleId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByRoleId", roleId);
    }

    @Override
    public int insert(UserRole userRole) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", userRole);
    }

    @Override
    public int deleteByUserId(String userId) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteByUserId", userId);
    }

    @Override
    public int deleteByUserIdAndRoleId(String userId, Long roleId) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("roleId", roleId);
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteByUserIdAndRoleId", params);
    }
}

