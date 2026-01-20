package com.hrp.cost.mapper.impl;

import com.hrp.cost.mapper.CostMainMapper;
import com.hrp.common.entity.CostMain;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class CostMainMapperImpl implements CostMainMapper {
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    private static final String NAMESPACE = "com.hrp.cost.mapper.CostMainMapper";

    @Override
    public CostMain selectById(Long id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }

    @Override
    public CostMain selectByNo(String costNo) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectByNo", costNo);
    }

    @Override
    public List<CostMain> selectAll() {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAll");
    }

    @Override
    public List<CostMain> selectByConditions(Long cycleId, Long deptId, String elementType, String startDate, String endDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("cycleId", cycleId);
        params.put("deptId", deptId);
        params.put("elementType", elementType);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByConditions", params);
    }

    @Override
    public List<CostMain> selectByDeptId(Long deptId, Long cycleId, String elementType) {
        Map<String, Object> params = new HashMap<>();
        params.put("deptId", deptId);
        params.put("cycleId", cycleId);
        params.put("elementType", elementType);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByDeptId", params);
    }

    @Override
    public long countByConditions(Long cycleId, Long deptId, String elementType, String startDate, String endDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("cycleId", cycleId);
        params.put("deptId", deptId);
        params.put("elementType", elementType);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        return sqlSessionTemplate.selectOne(NAMESPACE + ".countByConditions", params);
    }

    @Override
    public long countByDeptId(Long deptId, Long cycleId) {
        Map<String, Object> params = new HashMap<>();
        params.put("deptId", deptId);
        params.put("cycleId", cycleId);
        return sqlSessionTemplate.selectOne(NAMESPACE + ".countByDeptId", params);
    }

    @Override
    public String selectMaxCostNoByPrefix(String prefix) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectMaxCostNoByPrefix", prefix);
    }

    @Override
    public int insert(CostMain costMain) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", costMain);
    }

    @Override
    public int updateById(CostMain costMain) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", costMain);
    }

    @Override
    public int deleteById(Long id) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", id);
    }

    @Override
    public int batchInsert(List<CostMain> list) {
        return sqlSessionTemplate.insert(NAMESPACE + ".batchInsert", list);
    }
}

