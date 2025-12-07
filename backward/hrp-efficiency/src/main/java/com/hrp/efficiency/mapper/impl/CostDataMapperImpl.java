package com.hrp.efficiency.mapper.impl;

import com.hrp.efficiency.mapper.CostDataMapper;
import com.hrp.common.entity.CostData;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class CostDataMapperImpl implements CostDataMapper {
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    private static final String NAMESPACE = "com.hrp.efficiency.mapper.CostDataMapper";

    @Override
    public CostData selectById(Long id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }

    @Override
    public List<CostData> selectByDateRange(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByDateRange", params);
    }

    @Override
    public List<CostData> selectAll() {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAll");
    }

    @Override
    public int insert(CostData costData) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", costData);
    }

    @Override
    public int updateById(CostData costData) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", costData);
    }

    @Override
    public int deleteById(Long id) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", id);
    }
}

