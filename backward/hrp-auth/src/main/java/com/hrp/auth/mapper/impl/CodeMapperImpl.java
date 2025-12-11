package com.hrp.auth.mapper.impl;

import com.hrp.auth.mapper.CodeMapper;
import com.hrp.common.entity.Code;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * 系统字典数据访问实现类
 */
@Repository
public class CodeMapperImpl implements CodeMapper {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    private static final String NAMESPACE = "com.hrp.auth.mapper.CodeMapper";

    @Override
    public Code selectById(String id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }
}




