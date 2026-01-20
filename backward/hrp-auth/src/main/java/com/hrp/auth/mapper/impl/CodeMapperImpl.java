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
    public Code selectByCodeName(String codeName) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectByCodeName", codeName);
    }

    @Override
    public List<Code> selectByType(String codeType, Long isStop) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("codeType", codeType);
        params.put("isStop", isStop);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByType", params);
    }

    @Override
    public List<Code> selectAll(Long isStop) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAll", isStop);
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

    @Override
    public List<Code> selectAllPage(Long isStop, Long offset, Long size) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("isStop", isStop);
        params.put("offset", offset);
        params.put("size", size);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAllPage", params);
    }

    @Override
    public List<Code> selectByTypePage(String codeType, Long isStop, Long offset, Long size) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("codeType", codeType);
        params.put("isStop", isStop);
        params.put("offset", offset);
        params.put("size", size);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByTypePage", params);
    }

    @Override
    public Long countAll(Long isStop) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".countAll", isStop);
    }

    @Override
    public Long countByType(String codeType, Long isStop) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("codeType", codeType);
        params.put("isStop", isStop);
        return sqlSessionTemplate.selectOne(NAMESPACE + ".countByType", params);
    }
}













