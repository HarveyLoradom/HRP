package com.hrp.asset.mapper.impl;

import com.hrp.asset.mapper.ProcurementRequirementMapper;
import com.hrp.common.entity.ProcurementRequirement;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProcurementRequirementMapperImpl implements ProcurementRequirementMapper {
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    private static final String NAMESPACE = "com.hrp.asset.mapper.ProcurementRequirementMapper";

    @Override
    public ProcurementRequirement selectById(Long id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }

    @Override
    public List<ProcurementRequirement> selectByStatus(String status) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByStatus", status);
    }

    @Override
    public List<ProcurementRequirement> selectAll() {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAll");
    }

    @Override
    public int insert(ProcurementRequirement requirement) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", requirement);
    }

    @Override
    public int updateById(ProcurementRequirement requirement) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", requirement);
    }

    @Override
    public int deleteById(Long id) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", id);
    }
}

