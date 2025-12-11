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



