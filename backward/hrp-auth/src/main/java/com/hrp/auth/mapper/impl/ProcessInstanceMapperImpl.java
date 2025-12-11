package com.hrp.auth.mapper.impl;

import com.hrp.auth.mapper.ProcessInstanceMapper;
import com.hrp.common.entity.ProcessInstance;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 流程实例数据访问实现类
 */
@Repository
public class ProcessInstanceMapperImpl implements ProcessInstanceMapper {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    private static final String NAMESPACE = "com.hrp.auth.mapper.ProcessInstanceMapper";

    @Override
    public ProcessInstance selectById(Long instanceId) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", instanceId);
    }

    @Override
    public ProcessInstance selectByBusinessKey(String businessKey) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectByBusinessKey", businessKey);
    }

    @Override
    public ProcessInstance selectByBusiness(String businessType, Long businessId) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("businessType", businessType);
        params.put("businessId", businessId);
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectByBusiness", params);
    }

    @Override
    public List<ProcessInstance> selectAll() {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAll");
    }

    @Override
    public List<ProcessInstance> selectByStatus(String processStatus) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByStatus", processStatus);
    }

    @Override
    public int insert(ProcessInstance instance) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", instance);
    }

    @Override
    public int updateById(ProcessInstance instance) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", instance);
    }

    @Override
    public int deleteById(Long instanceId) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", instanceId);
    }
}



