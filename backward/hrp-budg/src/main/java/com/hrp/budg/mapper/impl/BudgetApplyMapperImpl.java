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
    public List<BudgetApply> selectPage(long offset, long size) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectPage", new java.util.HashMap<String, Object>() {{
            put("offset", offset);
            put("size", size);
        }});
    }

    @Override
    public long countAll() {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".countAll");
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

