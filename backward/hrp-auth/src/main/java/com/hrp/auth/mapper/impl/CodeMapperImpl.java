package com.hrp.auth.mapper.impl;

import com.hrp.auth.mapper.CodeMapper;
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

    private static final String NAMESPACE = "com.hrp.auth.mapper.CodeMapper";

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
        return sqlSessionTemplate.update(NAMESPACE + ".deleteById", id);
    }

    @Override
    public List<Code> selectAllPage(Long offset, Long size) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("offset", offset);
        params.put("size", size);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAllPage", params);
    }

    @Override
    public List<Code> selectByTypePage(String codeType, Long offset, Long size) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("codeType", codeType);
        params.put("offset", offset);
        params.put("size", size);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByTypePage", params);
    }

    @Override
    public Long countAll() {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".countAll");
    }

    @Override
    public Long countByType(String codeType) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".countByType", codeType);
    }
}













