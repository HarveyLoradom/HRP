package com.hrp.budg.mapper.impl;

import com.hrp.budg.mapper.BudgetDetailRecordMapper;
import com.hrp.common.entity.BudgetDetailRecord;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class BudgetDetailRecordMapperImpl implements BudgetDetailRecordMapper {
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    private static final String NAMESPACE = "com.hrp.budg.mapper.BudgetDetailRecordMapper";

    @Override
    public BudgetDetailRecord selectById(Long id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }

    @Override
    public List<BudgetDetailRecord> selectByBusinessNo(String businessNo) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByBusinessNo", businessNo);
    }

    @Override
    public List<BudgetDetailRecord> selectAppliesBySubjectAndItem(String subjectCode, String itemCode) {
        Map<String, Object> params = new HashMap<>();
        params.put("subjectCode", subjectCode);
        params.put("itemCode", itemCode);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAppliesBySubjectAndItem", params);
    }

    @Override
    public int insert(BudgetDetailRecord record) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", record);
    }

    @Override
    public int updateById(BudgetDetailRecord record) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", record);
    }

    @Override
    public int cancelByBusinessNo(String businessNo) {
        return sqlSessionTemplate.delete(NAMESPACE + ".cancelByBusinessNo", businessNo);
    }

    @Override
    public int cancelByBusinessId(Long businessId) {
        return sqlSessionTemplate.delete(NAMESPACE + ".cancelByBusinessId", businessId);
    }

    @Override
    public List<BudgetDetailRecord> selectByBusinessNoAndSubjectItemAndAmount(
            String businessNo, Long subjectId, Long itemId, java.math.BigDecimal amount) {
        Map<String, Object> params = new HashMap<>();
        params.put("businessNo", businessNo);
        params.put("subjectId", subjectId);
        params.put("itemId", itemId);
        params.put("amount", amount);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByBusinessNoAndSubjectItemAndAmount", params);
    }

    @Override
    public int deleteById(Long id) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", id);
    }
}

