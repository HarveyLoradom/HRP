package com.hrp.auth.mapper.impl;

import com.hrp.auth.mapper.UserMapper;
import com.hrp.common.entity.User;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户数据访问实现类
 */
@Repository
public class UserMapperImpl implements UserMapper {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    private static final String NAMESPACE = "com.hrp.auth.mapper.UserMapper";

    @Override
    public User selectById(String id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }

    @Override
    public User selectByAccount(String account) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectByAccount", account);
    }

    @Override
    public int insert(User user) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", user);
    }

    @Override
    public int updateById(User user) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", user);
    }

    @Override
    public int deleteById(String id) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", id);
    }

    @Override
    public List<User> selectAll() {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAll");
    }
}

