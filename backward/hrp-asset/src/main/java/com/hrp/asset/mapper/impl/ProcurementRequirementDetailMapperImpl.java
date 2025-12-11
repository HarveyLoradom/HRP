package com.hrp.asset.mapper.impl;

import com.hrp.asset.mapper.ProcurementRequirementDetailMapper;
import com.hrp.common.entity.ProcurementRequirementDetail;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProcurementRequirementDetailMapperImpl implements ProcurementRequirementDetailMapper {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    private static final String NAMESPACE = "com.hrp.asset.mapper.ProcurementRequirementDetailMapper";

    @Override
    public ProcurementRequirementDetail selectById(Long id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }

    @Override
    public List<ProcurementRequirementDetail> selectByRequirementId(Long requirementId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByRequirementId", requirementId);
    }

    @Override
    public int insert(ProcurementRequirementDetail detail) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", detail);
    }

    @Override
    public int updateById(ProcurementRequirementDetail detail) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", detail);
    }

    @Override
    public int deleteById(Long id) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", id);
    }

    @Override
    public int deleteByRequirementId(Long requirementId) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteByRequirementId", requirementId);
    }
}

