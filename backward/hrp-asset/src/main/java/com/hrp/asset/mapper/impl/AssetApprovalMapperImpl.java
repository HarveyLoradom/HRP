package com.hrp.asset.mapper.impl;

import com.hrp.asset.mapper.AssetApprovalMapper;
import com.hrp.common.entity.AssetApproval;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AssetApprovalMapperImpl implements AssetApprovalMapper {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    private static final String NAMESPACE = "com.hrp.asset.mapper.AssetApprovalMapper";

    @Override
    public AssetApproval selectById(Long id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }

    @Override
    public AssetApproval selectByApprovalNo(String approvalNo) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectByApprovalNo", approvalNo);
    }

    @Override
    public List<AssetApproval> selectByType(String approvalType) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByType", approvalType);
    }

    @Override
    public List<AssetApproval> selectByApplicant(Long applicantId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByApplicant", applicantId);
    }

    @Override
    public List<AssetApproval> selectByApprover(String approverId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByApprover", approverId);
    }

    @Override
    public List<AssetApproval> selectAll() {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAll");
    }

    @Override
    public List<AssetApproval> selectByStatus(String status) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByStatus", status);
    }

    @Override
    public int insert(AssetApproval assetApproval) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", assetApproval);
    }

    @Override
    public int updateById(AssetApproval assetApproval) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", assetApproval);
    }

    @Override
    public int deleteById(Long id) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", id);
    }
}












