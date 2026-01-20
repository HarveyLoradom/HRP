package com.hrp.auth.mapper.impl;

import com.hrp.auth.mapper.ProcessDefinitionMapper;
import com.hrp.common.entity.ProcessDefinition;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 流程定义数据访问实现类
 */
@Repository
public class ProcessDefinitionMapperImpl implements ProcessDefinitionMapper {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    private static final String NAMESPACE = "com.hrp.auth.mapper.ProcessDefinitionMapper";

    @Override
    public ProcessDefinition selectById(Long definitionId) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", definitionId);
    }

    @Override
    public ProcessDefinition selectByKey(String definitionKey) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectByKey", definitionKey);
    }

    @Override
    public List<ProcessDefinition> selectByType(String definitionType, Long isActive) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("definitionType", definitionType);
        params.put("isActive", isActive);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByType", params);
    }

    @Override
    public List<ProcessDefinition> selectAll(Long isActive) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAll", isActive);
    }

    @Override
    public List<ProcessDefinition> selectAllPage(Long isActive, Long offset, Long size) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("isActive", isActive);
        params.put("offset", offset);
        params.put("size", size);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAllPage", params);
    }

    @Override
    public Long countAll(Long isActive) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".countAll", isActive);
    }

    @Override
    public List<ProcessDefinition> selectByTypePage(String definitionType, Long isActive, Long offset, Long size) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("definitionType", definitionType);
        params.put("isActive", isActive);
        params.put("offset", offset);
        params.put("size", size);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByTypePage", params);
    }

    @Override
    public Long countByType(String definitionType, Long isActive) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("definitionType", definitionType);
        params.put("isActive", isActive);
        return sqlSessionTemplate.selectOne(NAMESPACE + ".countByType", params);
    }

    @Override
    public int insert(ProcessDefinition definition) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", definition);
    }

    @Override
    public int updateById(ProcessDefinition definition) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", definition);
    }

    @Override
    public int deleteById(Long definitionId) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", definitionId);
    }
}



