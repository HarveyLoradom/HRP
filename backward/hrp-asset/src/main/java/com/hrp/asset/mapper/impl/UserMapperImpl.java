package com.hrp.asset.mapper.impl;

import com.hrp.asset.mapper.UserMapper;
import com.hrp.common.entity.User;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * 用户数据访问实现类
 */
@Repository
public class UserMapperImpl implements UserMapper {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    private static final String NAMESPACE = "com.hrp.asset.mapper.UserMapper";

    @Override
    public User selectById(String id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }
}







