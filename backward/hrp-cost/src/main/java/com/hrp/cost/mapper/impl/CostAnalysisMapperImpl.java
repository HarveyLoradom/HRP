package com.hrp.cost.mapper.impl;

import com.hrp.cost.mapper.CostAnalysisMapper;
import com.hrp.common.entity.CostAnalysis;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CostAnalysisMapperImpl implements CostAnalysisMapper {
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    private static final String NAMESPACE = "com.hrp.cost.mapper.CostAnalysisMapper";

    @Override
    public CostAnalysis selectById(Long id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }

    @Override
    public List<CostAnalysis> selectAll() {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAll");
    }

    @Override
    public int insert(CostAnalysis costAnalysis) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", costAnalysis);
    }

    @Override
    public int updateById(CostAnalysis costAnalysis) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", costAnalysis);
    }

    @Override
    public int deleteById(Long id) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", id);
    }
}

