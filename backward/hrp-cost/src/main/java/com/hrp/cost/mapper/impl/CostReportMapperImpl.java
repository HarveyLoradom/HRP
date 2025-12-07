package com.hrp.cost.mapper.impl;

import com.hrp.cost.mapper.CostReportMapper;
import com.hrp.common.entity.CostReport;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CostReportMapperImpl implements CostReportMapper {
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    private static final String NAMESPACE = "com.hrp.cost.mapper.CostReportMapper";

    @Override
    public CostReport selectById(Long id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }

    @Override
    public List<CostReport> selectByPeriod(String reportPeriod) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByPeriod", reportPeriod);
    }

    @Override
    public List<CostReport> selectAll() {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAll");
    }

    @Override
    public int insert(CostReport costReport) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", costReport);
    }

    @Override
    public int updateById(CostReport costReport) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", costReport);
    }

    @Override
    public int deleteById(Long id) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", id);
    }
}

