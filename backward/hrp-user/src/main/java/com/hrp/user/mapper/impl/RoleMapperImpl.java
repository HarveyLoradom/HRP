package com.hrp.user.mapper.impl;

import com.hrp.user.mapper.RoleMapper;
import com.hrp.common.entity.Role;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 角色数据访问实现类
 */
@Repository
public class RoleMapperImpl implements RoleMapper {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    private static final String NAMESPACE = "com.hrp.user.mapper.RoleMapper";

    @Override
    public Role selectById(Long roleId) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", roleId);
    }

    @Override
    public Role selectByCode(String roleCode) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectByCode", roleCode);
    }

    @Override
    public List<Role> selectAll() {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAll");
    }

    @Override
    public int insert(Role role) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", role);
    }

    @Override
    public int updateById(Role role) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", role);
    }

    @Override
    public int deleteById(Long roleId) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", roleId);
    }
}
