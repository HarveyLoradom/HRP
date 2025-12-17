package com.hrp.budg.mapper.impl;

import com.hrp.budg.mapper.BudgetMapper;
import com.hrp.common.entity.Budget;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public class BudgetMapperImpl implements BudgetMapper {
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    private static final String NAMESPACE = "com.hrp.budg.mapper.BudgetMapper";

    @Override
    public Budget selectById(Long id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }

    @Override
    public Budget selectByNo(String no) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectByNo", no);
    }

    @Override
    public List<Budget> selectAll() {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAll");
    }

    @Override
    public List<Budget> selectByYear(String year) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByYear", year);
    }

    @Override
    public List<Budget> selectBySubjectId(Long subjectId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectBySubjectId", subjectId);
    }

    @Override
    public List<Budget> selectByItemId(Long itemId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByItemId", itemId);
    }

    @Override
    public List<Budget> selectBySubjectAndItem(Long subjectId, Long itemId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectBySubjectAndItem", new java.util.HashMap<String, Object>() {{
            put("subjectId", subjectId);
            put("itemId", itemId);
        }});
    }

    @Override
    public int insert(Budget budget) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", budget);
    }

    @Override
    public int updateById(Budget budget) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", budget);
    }

    @Override
    public int deleteById(Long id) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", id);
    }

    @Override
    public int updateExecutionAmount(Long id, BigDecimal amount) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateExecutionAmount", new java.util.HashMap<String, Object>() {{
            put("id", id);
            put("amount", amount);
        }});
    }
}













