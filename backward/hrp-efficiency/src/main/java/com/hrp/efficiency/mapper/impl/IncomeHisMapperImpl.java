package com.hrp.efficiency.mapper.impl;

import com.hrp.efficiency.mapper.IncomeHisMapper;
import com.hrp.common.entity.IncomeHis;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class IncomeHisMapperImpl implements IncomeHisMapper {
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    private static final String NAMESPACE = "com.hrp.efficiency.mapper.IncomeHisMapper";

    @Override
    public IncomeHis selectById(Long id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }

    @Override
    public List<IncomeHis> selectByDateRange(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByDateRange", params);
    }

    @Override
    public List<IncomeHis> selectAll() {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAll");
    }

    @Override
    public int insert(IncomeHis incomeHis) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", incomeHis);
    }

    @Override
    public int updateById(IncomeHis incomeHis) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", incomeHis);
    }

    @Override
    public int deleteById(Long id) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", id);
    }
}

