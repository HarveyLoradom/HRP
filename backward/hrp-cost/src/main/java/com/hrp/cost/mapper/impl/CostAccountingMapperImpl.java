package com.hrp.cost.mapper.impl;

import com.hrp.cost.mapper.CostAccountingMapper;
import com.hrp.common.entity.CostAccounting;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CostAccountingMapperImpl implements CostAccountingMapper {
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    private static final String NAMESPACE = "com.hrp.cost.mapper.CostAccountingMapper";

    @Override
    public CostAccounting selectById(Long id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }

    @Override
    public List<CostAccounting> selectByPeriod(String accountingPeriod) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByPeriod", accountingPeriod);
    }

    @Override
    public List<CostAccounting> selectAll() {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAll");
    }

    @Override
    public int insert(CostAccounting costAccounting) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", costAccounting);
    }

    @Override
    public int updateById(CostAccounting costAccounting) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", costAccounting);
    }

    @Override
    public int deleteById(Long id) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", id);
    }
}

