package com.hrp.contract.mapper.impl;

import com.hrp.contract.mapper.PactMainMapper;
import com.hrp.common.entity.PactMain;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class PactMainMapperImpl implements PactMainMapper {
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    private static final String NAMESPACE = "com.hrp.contract.mapper.PactMainMapper";

    @Override
    public PactMain selectById(Long id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }

    @Override
    public PactMain selectByContractNo(String contractNo) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectByContractNo", contractNo);
    }

    @Override
    public List<PactMain> selectByStatus(String status) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByStatus", status);
    }

    @Override
    public List<PactMain> selectAll() {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAll");
    }

    @Override
    public List<PactMain> selectAllPage(Long offset, Long size) {
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
    public List<PactMain> selectByStatusPage(String status, Long offset, Long size) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("status", status);
        params.put("offset", offset);
        params.put("size", size);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByStatusPage", params);
    }

    @Override
    public Long countByStatus(String status) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".countByStatus", status);
    }

    @Override
    public int insert(PactMain pactMain) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", pactMain);
    }

    @Override
    public int updateById(PactMain pactMain) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", pactMain);
    }

    @Override
    public int deleteById(Long id) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", id);
    }

    @Override
    public List<PactMain> selectByApprover(String userId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByApprover", userId);
    }

    @Override
    public String selectMaxContractNoByPrefix(String prefix) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectMaxContractNoByPrefix", prefix);
    }

    @Override
    public List<PactMain> selectByConditions(Long empId, String contractNo, String contractName, String contractType, String status, String startDate, String endDate) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("empId", empId);
        params.put("contractNo", contractNo);
        params.put("contractName", contractName);
        params.put("contractType", contractType);
        params.put("status", status);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByConditions", params);
    }

    @Override
    public int updateExecutionStatusToExecuting(LocalDateTime now) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateExecutionStatusToExecuting", now);
    }

    @Override
    public int updateExecutionStatusToCompleted(LocalDateTime now) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateExecutionStatusToCompleted", now);
    }

    @Override
    public List<PactMain> selectApprovedContractsByConditions(String contractNo, String contractName, String contractType, String executionStatus) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("contractNo", contractNo);
        params.put("contractName", contractName);
        params.put("contractType", contractType);
        params.put("executionStatus", executionStatus);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectApprovedContractsByConditions", params);
    }
}

