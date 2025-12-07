package com.hrp.user.mapper.impl;

import com.hrp.user.mapper.CodeMapper;
import com.hrp.common.entity.Code;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 系统字典数据访问实现类
 */
@Repository
public class CodeMapperImpl implements CodeMapper {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    private static final String NAMESPACE = "com.hrp.user.mapper.CodeMapper";

    @Override
    public Code selectById(String id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }

    @Override
    public List<Code> selectByType(String codeType) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByType", codeType);
    }

    @Override
    public List<Code> selectAll() {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAll");
    }

    @Override
    public int insert(Code code) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", code);
    }

    @Override
    public int updateById(Code code) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", code);
    }

    @Override
    public int deleteById(String id) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", id);
    }
}
