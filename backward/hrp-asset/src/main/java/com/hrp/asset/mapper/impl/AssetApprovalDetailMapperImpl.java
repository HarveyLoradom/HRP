package com.hrp.asset.mapper.impl;

import com.hrp.asset.mapper.AssetApprovalDetailMapper;
import com.hrp.common.entity.AssetApprovalDetail;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AssetApprovalDetailMapperImpl implements AssetApprovalDetailMapper {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    private static final String NAMESPACE = "com.hrp.asset.mapper.AssetApprovalDetailMapper";

    @Override
    public AssetApprovalDetail selectById(Long id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }

    @Override
    public List<AssetApprovalDetail> selectByApprovalId(Long approvalId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByApprovalId", approvalId);
    }

    @Override
    public int insert(AssetApprovalDetail detail) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", detail);
    }

    @Override
    public int updateById(AssetApprovalDetail detail) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", detail);
    }

    @Override
    public int deleteById(Long id) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", id);
    }

    @Override
    public int deleteByApprovalId(Long approvalId) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteByApprovalId", approvalId);
    }
}







