package com.hrp.hr.mapper.impl;

import com.hrp.hr.mapper.HrApplyMapper;
import com.hrp.common.entity.HrApply;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 业务申请表数据访问实现类
 */
@Repository
public class HrApplyMapperImpl implements HrApplyMapper {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    private static final String NAMESPACE = "com.hrp.hr.mapper.HrApplyMapper";

    @Override
    public HrApply selectById(Long applyId) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", applyId);
    }

    @Override
    public HrApply selectByApplyNo(String applyNo) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectByApplyNo", applyNo);
    }

    @Override
    public List<HrApply> selectByEmpId(Long empId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByEmpId", empId);
    }

    @Override
    public List<HrApply> selectByEmpIdPage(Long empId, Long offset, Long size) {
        Map<String, Object> params = new HashMap<>();
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
    public List<HrApply> selectByStatus(String status) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByStatus", status);
    }

    @Override
    public List<HrApply> selectByStatusPage(String status, Long offset, Long size) {
        Map<String, Object> params = new HashMap<>();
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
    public List<HrApply> selectAll() {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAll");
    }

    @Override
    public List<HrApply> selectAllPage(Long offset, Long size) {
        Map<String, Object> params = new HashMap<>();
        params.put("offset", offset);
        params.put("size", size);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAllPage", params);
    }

    @Override
    public Long countAll() {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".countAll");
    }

    @Override
    public List<HrApply> selectByApprover(String userId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByApprover", userId);
    }

    @Override
    public List<HrApply> selectByApproverPage(String userId, String status, Long offset, Long size) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("status", status);
        params.put("offset", offset);
        params.put("size", size);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByApproverPage", params);
    }

    @Override
    public Long countByApprover(String userId, String status) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("status", status);
        return sqlSessionTemplate.selectOne(NAMESPACE + ".countByApprover", params);
    }

    @Override
    public List<HrApply> selectByConditions(String applyNo, Long empId, String empName, String hrApplyType, 
                                             String status, String startDate, String endDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("applyNo", applyNo);
        params.put("empId", empId);
        params.put("empName", empName);
        params.put("hrApplyType", hrApplyType);
        params.put("status", status);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByConditions", params);
    }

    @Override
    public String selectMaxApplyNoByPrefix(String prefix) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectMaxApplyNoByPrefix", prefix);
    }

    @Override
    public int insert(HrApply hrApply) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", hrApply);
    }

    @Override
    public int updateById(HrApply hrApply) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", hrApply);
    }

    @Override
    public int deleteById(Long applyId) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", applyId);
    }
}

