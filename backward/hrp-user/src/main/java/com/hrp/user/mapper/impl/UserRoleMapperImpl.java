package com.hrp.user.mapper.impl;

import com.hrp.user.mapper.UserRoleMapper;
import com.hrp.common.entity.UserRole;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户角色关联数据访问实现类
 */
@Repository
public class UserRoleMapperImpl implements UserRoleMapper {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    private static final String NAMESPACE = "com.hrp.user.mapper.UserRoleMapper";

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
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteByUserIdAndRoleId", userRole);
    }
}
