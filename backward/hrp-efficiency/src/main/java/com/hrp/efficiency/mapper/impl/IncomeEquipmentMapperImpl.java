package com.hrp.efficiency.mapper.impl;

import com.hrp.efficiency.mapper.IncomeEquipmentMapper;
import com.hrp.common.entity.IncomeEquipment;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class IncomeEquipmentMapperImpl implements IncomeEquipmentMapper {
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    private static final String NAMESPACE = "com.hrp.efficiency.mapper.IncomeEquipmentMapper";

    @Override
    public IncomeEquipment selectById(Long id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }

    @Override
    public List<IncomeEquipment> selectByEquipmentId(Long equipmentId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByEquipmentId", equipmentId);
    }

    @Override
    public List<IncomeEquipment> selectByDateRange(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByDateRange", params);
    }

    @Override
    public List<IncomeEquipment> selectAll() {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAll");
    }

    @Override
    public int insert(IncomeEquipment incomeEquipment) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", incomeEquipment);
    }

    @Override
    public int updateById(IncomeEquipment incomeEquipment) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", incomeEquipment);
    }

    @Override
    public int deleteById(Long id) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", id);
    }
}

