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
    public List<CtrlPayout> selectByItemId(Long itemId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByItemId", itemId);
    }

    @Override
    public List<CtrlPayout> selectPayoutByItemId(Long itemId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectPayoutByItemId", itemId);
    }

    @Override
    public List<CtrlPayout> selectByApprover(String userId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByApprover", userId);
    }

    @Override
    public List<CtrlPayout> selectByApproverPage(String userId, String status, Long offset, Long size) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("userId", userId);
        params.put("status", status);
        params.put("offset", offset);
        params.put("size", size);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByApproverPage", params);
    }

    @Override
    public Long countByApprover(String userId, String status) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("userId", userId);
        params.put("status", status);
        return sqlSessionTemplate.selectOne(NAMESPACE + ".countByApprover", params);
    }

    @Override
    public String selectMaxBillcodeByPrefix(String prefix) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectMaxBillcodeByPrefix", prefix);
    }

    @Override
    public List<CtrlPayout> selectByConditions(String payoutBillcode, String empName, String payoutTypeId, String status, String startDate, String endDate, String billTypePrefix) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("payoutBillcode", payoutBillcode);
        params.put("empName", empName);
        params.put("payoutTypeId", payoutTypeId);
        params.put("status", status);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("billTypePrefix", billTypePrefix);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByConditions", params);
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

    @Override
    public CtrlPayout selectBySourceApplyNo(String sourceApplyNo, Long excludePayoutId) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("sourceApplyNo", sourceApplyNo);
        params.put("excludePayoutId", excludePayoutId);
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectBySourceApplyNo", params);
    }

    @Override
    public CtrlPayout selectByContractNo(String contractNo, Long excludePayoutId) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("contractNo", contractNo);
        params.put("excludePayoutId", excludePayoutId);
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectByContractNo", params);
    }
}




