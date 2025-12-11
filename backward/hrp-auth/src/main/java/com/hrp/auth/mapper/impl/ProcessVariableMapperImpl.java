package com.hrp.auth.mapper.impl;

import com.hrp.auth.mapper.ProcessVariableMapper;
import com.hrp.common.entity.ProcessVariable;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 流程变量数据访问实现类
 */
@Repository
public class ProcessVariableMapperImpl implements ProcessVariableMapper {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    private static final String NAMESPACE = "com.hrp.auth.mapper.ProcessVariableMapper";

    @Override
    public List<ProcessVariable> selectByInstanceId(Long processInstanceId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByInstanceId", processInstanceId);
    }

    @Override
    public ProcessVariable selectByInstanceIdAndKey(Long processInstanceId, String variableKey) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("processInstanceId", processInstanceId);
        params.put("variableKey", variableKey);
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectByInstanceIdAndKey", params);
    }

    @Override
    public int insert(ProcessVariable variable) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", variable);
    }

    @Override
    public int updateById(ProcessVariable variable) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", variable);
    }

    @Override
    public int deleteById(Long variableId) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", variableId);
    }

    @Override
    public int deleteByInstanceId(Long instanceId) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteByInstanceId", instanceId);
    }
}



