package com.hrp.budg.mapper.impl;

import com.hrp.budg.mapper.BudgetCategoryMapper;
import com.hrp.common.entity.BudgetCategory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class BudgetCategoryMapperImpl implements BudgetCategoryMapper {
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    private static final String NAMESPACE = "com.hrp.budg.mapper.BudgetCategoryMapper";

    @Override
    public List<BudgetCategory> selectLevel1ByConditions(String budgetYear, String categoryType) {
        Map<String, Object> params = new HashMap<>();
        params.put("budgetYear", budgetYear);
        params.put("categoryType", categoryType);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectLevel1ByConditions", params);
    }

    @Override
    public List<BudgetCategory> selectLevel2ByConditions(String budgetYear, String categoryType, Long parentCategoryId) {
        Map<String, Object> params = new HashMap<>();
        params.put("budgetYear", budgetYear);
        params.put("categoryType", categoryType);
        params.put("parentCategoryId", parentCategoryId);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectLevel2ByConditions", params);
    }

    @Override
    public BudgetCategory selectById(Long id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }

    @Override
    public BudgetCategory selectByCode(String code) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectByCode", code);
    }

    @Override
    public int insert(BudgetCategory category) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", category);
    }

    @Override
    public int updateById(BudgetCategory category) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", category);
    }

    @Override
    public int deleteById(Long id) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", id);
    }

    @Override
    public int countLevel2ByParentId(Long parentCategoryId) {
        Integer count = sqlSessionTemplate.selectOne(NAMESPACE + ".countLevel2ByParentId", parentCategoryId);
        return count != null ? count : 0;
    }

    @Override
    public int countItemsByCategoryId(Long categoryId) {
        Integer count = sqlSessionTemplate.selectOne(NAMESPACE + ".countItemsByCategoryId", categoryId);
        return count != null ? count : 0;
    }

    @Override
    public String selectMaxLevel1Code(String budgetYear, String categoryType) {
        Map<String, Object> params = new HashMap<>();
        params.put("budgetYear", budgetYear);
        params.put("categoryType", categoryType);
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectMaxLevel1Code", params);
    }

    @Override
    public String selectMaxLevel2Code(String parentCategoryCode) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectMaxLevel2Code", parentCategoryCode);
    }
}

