package com.hrp.hr.mapper.impl;

import com.hrp.hr.mapper.BusinessApplyMapper;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class BusinessApplyMapperImpl implements BusinessApplyMapper {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    private static final String NAMESPACE = "com.hrp.hr.mapper.BusinessApplyMapper";

    @Override
    public List<?> selectAll() {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAll");
    }

    @Override
    public List<?> selectByApplicant(Long applicantId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByApplicant", applicantId);
    }

    @Override
    public List<?> selectByApprover(String approverId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByApprover", approverId);
    }

    @Override
    public Object selectById(Long id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }

    @Override
    public int insert(Map<String, Object> data) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", data);
    }

    @Override
    public int updateById(Map<String, Object> data) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", data);
    }

    @Override
    public int deleteById(Long id) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", id);
    }

    @Override
    public List<?> selectRecordsByApplyId(Long applyId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectRecordsByApplyId", applyId);
    }
}





