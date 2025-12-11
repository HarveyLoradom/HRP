package com.hrp.asset.mapper.impl;

import com.hrp.asset.mapper.ProcurementRequirementApprovalMapper;
import com.hrp.common.entity.ProcurementRequirementApproval;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProcurementRequirementApprovalMapperImpl implements ProcurementRequirementApprovalMapper {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    private static final String NAMESPACE = "com.hrp.asset.mapper.ProcurementRequirementApprovalMapper";

    @Override
    public ProcurementRequirementApproval selectById(Long id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }

    @Override
    public List<ProcurementRequirementApproval> selectByRequirementId(Long requirementId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByRequirementId", requirementId);
    }

    @Override
    public int insert(ProcurementRequirementApproval approval) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", approval);
    }
}
