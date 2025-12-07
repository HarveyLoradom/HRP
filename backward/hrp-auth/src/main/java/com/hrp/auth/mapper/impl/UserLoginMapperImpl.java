package com.hrp.auth.mapper.impl;

import com.hrp.auth.mapper.UserLoginMapper;
import com.hrp.common.entity.UserLogin;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * 用户登录信息数据访问实现类
 */
@Repository
public class UserLoginMapperImpl implements UserLoginMapper {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    private static final String NAMESPACE = "com.hrp.auth.mapper.UserLoginMapper";

    @Override
    public UserLogin selectByUserId(String userId) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectByUserId", userId);
    }

    @Override
    public int insert(UserLogin userLogin) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", userLogin);
    }

    @Override
    public int updateById(UserLogin userLogin) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", userLogin);
    }
}

