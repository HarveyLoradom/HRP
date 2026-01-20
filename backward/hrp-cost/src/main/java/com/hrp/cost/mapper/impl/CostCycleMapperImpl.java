package com.hrp.cost.mapper.impl;

import com.hrp.cost.mapper.CostCycleMapper;
import com.hrp.common.entity.CostCycle;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class CostCycleMapperImpl implements CostCycleMapper {
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    private static final String NAMESPACE = "com.hrp.cost.mapper.CostCycleMapper";

    @Override
    public CostCycle selectById(Long id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }

    @Override
    public CostCycle selectByCode(String cycleCode) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectByCode", cycleCode);
    }

    @Override
    public List<CostCycle> selectAll() {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAll");
    }

    @Override
    public List<CostCycle> selectByConditions(String cycleCode, String cycleName, String cycleType, Integer status) {
        Map<String, Object> params = new HashMap<>();
        params.put("cycleCode", cycleCode);
        params.put("cycleName", cycleName);
        params.put("cycleType", cycleType);
        params.put("status", status);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByConditions", params);
    }

    @Override
    public int insert(CostCycle costCycle) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", costCycle);
    }

    @Override
    public int updateById(CostCycle costCycle) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", costCycle);
    }

    @Override
    public int deleteById(Long id) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", id);
    }
}

