package com.hrp.budg.mapper.impl;

import com.hrp.budg.mapper.BudgetApplyMapper;
import com.hrp.common.entity.BudgetApply;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BudgetApplyMapperImpl implements BudgetApplyMapper {
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    private static final String NAMESPACE = "com.hrp.budg.mapper.BudgetApplyMapper";

    @Override
    public BudgetApply selectById(Long id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }

    @Override
    public BudgetApply selectByNo(String applyNo) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectByNo", applyNo);
    }

    @Override
    public List<BudgetApply> selectAll() {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAll");
    }

    @Override
    public List<BudgetApply> selectPage(long offset, long size) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectPage", new java.util.HashMap<String, Object>() {{
            put("offset", offset);
            put("size", size);
        }});
    }

    @Override
    public List<BudgetApply> selectByItemId(Long itemId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByItemId", itemId);
    }

    @Override
    public List<BudgetApply> selectByConditions(String applyNo, Long itemId, String applicantName, String applicantCode, String status, String startDate, String endDate) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("applyNo", applyNo);
        params.put("itemId", itemId);
        params.put("applicantName", applicantName);
        params.put("applicantCode", applicantCode);
        params.put("status", status);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByConditions", params);
    }

    @Override
    public long countAll() {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".countAll");
    }

    @Override
    public String selectMaxApplyNoByPrefix(String prefix) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectMaxApplyNoByPrefix", prefix);
    }

    @Override
    public BudgetApply selectByItemSubjectYear(Long itemId, Long subjectId, String budgetYear, Long excludeApplyId) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("itemId", itemId);
        params.put("subjectId", subjectId);
        params.put("budgetYear", budgetYear);
        params.put("excludeApplyId", excludeApplyId);
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectByItemSubjectYear", params);
    }

    @Override
    public int insert(BudgetApply budgetApply) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", budgetApply);
    }

    @Override
    public int updateById(BudgetApply budgetApply) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", budgetApply);
    }

    @Override
    public int deleteById(Long id) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", id);
    }
}

