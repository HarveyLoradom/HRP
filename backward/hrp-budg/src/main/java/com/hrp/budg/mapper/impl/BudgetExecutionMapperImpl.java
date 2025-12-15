package com.hrp.budg.mapper.impl;

import com.hrp.budg.mapper.BudgetExecutionMapper;
import com.hrp.common.entity.BudgetExecution;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class BudgetExecutionMapperImpl implements BudgetExecutionMapper {
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    private static final String NAMESPACE = "com.hrp.budg.mapper.BudgetExecutionMapper";

    @Override
    public BudgetExecution selectById(Long id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }

    @Override
    public BudgetExecution selectByNo(String no) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectByNo", no);
    }

    @Override
    public List<BudgetExecution> selectAll() {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAll");
    }

    @Override
    public List<BudgetExecution> selectByBudgetId(Long budgetId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByBudgetId", budgetId);
    }

    @Override
    public List<BudgetExecution> selectByBusinessId(String businessType, Long businessId) {
        Map<String, Object> params = new HashMap<>();
        params.put("businessType", businessType);
        params.put("businessId", businessId);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByBusinessId", params);
    }

    @Override
    public int insert(BudgetExecution budgetExecution) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", budgetExecution);
    }

    @Override
    public int updateById(BudgetExecution budgetExecution) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", budgetExecution);
    }

    @Override
    public int deleteById(Long id) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", id);
    }
}








