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
    public List<ProcessDefinition> selectByType(String definitionType) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByType", definitionType);
    }

    @Override
    public List<ProcessDefinition> selectAll() {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAll");
    }

    @Override
    public List<ProcessDefinition> selectAllPage(Long offset, Long size) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("offset", offset);
        params.put("size", size);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAllPage", params);
    }

    @Override
    public Long countAll() {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".countAll");
    }

    @Override
    public List<ProcessDefinition> selectByTypePage(String definitionType, Long offset, Long size) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("definitionType", definitionType);
        params.put("offset", offset);
        params.put("size", size);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByTypePage", params);
    }

    @Override
    public Long countByType(String definitionType) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".countByType", definitionType);
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



