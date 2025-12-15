package com.hrp.reimb.mapper.impl;

import com.hrp.reimb.mapper.CtrlPayoutMapper;
import com.hrp.common.entity.CtrlPayout;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CtrlPayoutMapperImpl implements CtrlPayoutMapper {
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    private static final String NAMESPACE = "com.hrp.reimb.mapper.CtrlPayoutMapper";

    @Override
    public CtrlPayout selectById(Long id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }

    @Override
    public CtrlPayout selectByBillcode(String billcode) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectByBillcode", billcode);
    }

    @Override
    public List<CtrlPayout> selectByEmpId(Long empId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByEmpId", empId);
    }

    @Override
    public List<CtrlPayout> selectByEmpIdPage(Long empId, Long offset, Long size) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("empId", empId);
        params.put("offset", offset);
        params.put("size", size);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByEmpIdPage", params);
    }

    @Override
    public Long countByEmpId(Long empId) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".countByEmpId", empId);
    }

    @Override
    public List<CtrlPayout> selectByStatus(String status) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByStatus", status);
    }

    @Override
    public List<CtrlPayout> selectByStatusPage(String status, Long offset, Long size) {
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
    public List<CtrlPayout> selectAll() {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAll");
    }

    @Override
    public List<CtrlPayout> selectAllPage(Long offset, Long size) {
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
    public List<CtrlPayout> selectByApprover(String userId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByApprover", userId);
    }

    @Override
    public List<CtrlPayout> selectByApproverPage(String userId, Long offset, Long size) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("userId", userId);
        params.put("offset", offset);
        params.put("size", size);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByApproverPage", params);
    }

    @Override
    public Long countByApprover(String userId) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".countByApprover", userId);
    }

    @Override
    public int insert(CtrlPayout ctrlPayout) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", ctrlPayout);
    }

    @Override
    public int updateById(CtrlPayout ctrlPayout) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", ctrlPayout);
    }

    @Override
    public int deleteById(Long id) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", id);
    }
}

