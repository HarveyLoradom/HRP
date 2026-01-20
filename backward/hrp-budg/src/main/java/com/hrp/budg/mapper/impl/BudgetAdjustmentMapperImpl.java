package com.hrp.budg.mapper.impl;

import com.hrp.budg.mapper.BudgetAdjustmentMapper;
import com.hrp.common.entity.BudgetAdjustment;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class BudgetAdjustmentMapperImpl implements BudgetAdjustmentMapper {
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    private static final String NAMESPACE = "com.hrp.budg.mapper.BudgetAdjustmentMapper";

    @Override
    public BudgetAdjustment selectById(Long id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }

    @Override
    public BudgetAdjustment selectByNo(String no) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectByNo", no);
    }

    @Override
    public List<BudgetAdjustment> selectAll() {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAll");
    }

    @Override
    public List<BudgetAdjustment> selectPage(int offset, int size, Map<String, Object> params) {
        Map<String, Object> map = new HashMap<>();
        map.put("offset", offset);
        map.put("size", size);
        map.put("params", params != null ? params : new HashMap<>());
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectPage", map);
    }

    @Override
    public long countByConditions(Map<String, Object> params) {
        Map<String, Object> map = new HashMap<>();
        map.put("params", params != null ? params : new HashMap<>());
        Long count = sqlSessionTemplate.selectOne(NAMESPACE + ".countByConditions", map);
        return count != null ? count : 0L;
    }

    @Override
    public List<BudgetAdjustment> selectByItemId(Long itemId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByItemId", itemId);
    }

    @Override
    public int insert(BudgetAdjustment budgetAdjustment) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", budgetAdjustment);
    }

    @Override
    public int updateById(BudgetAdjustment budgetAdjustment) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", budgetAdjustment);
    }

    @Override
    public int deleteById(Long id) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", id);
    }
}

