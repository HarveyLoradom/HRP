package com.hrp.budg.mapper.impl;

import com.hrp.budg.mapper.BudgetItemMapper;
import com.hrp.common.entity.BudgetItem;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BudgetItemMapperImpl implements BudgetItemMapper {
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    private static final String NAMESPACE = "com.hrp.budg.mapper.BudgetItemMapper";

    @Override
    public BudgetItem selectById(Long id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }

    @Override
    public BudgetItem selectByCode(String code) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectByCode", code);
    }

    @Override
    public List<BudgetItem> selectAll() {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAll");
    }

    @Override
    public List<BudgetItem> selectByCategoryId(Long categoryId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByCategoryId", categoryId);
    }

    @Override
    public int insert(BudgetItem budgetItem) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", budgetItem);
    }

    @Override
    public int updateById(BudgetItem budgetItem) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", budgetItem);
    }

    @Override
    public int deleteById(Long id) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", id);
    }
}








