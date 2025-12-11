package com.hrp.asset.mapper.impl;

import com.hrp.asset.mapper.AssetApprovalRecordMapper;
import com.hrp.common.entity.AssetApprovalRecord;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AssetApprovalRecordMapperImpl implements AssetApprovalRecordMapper {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    private static final String NAMESPACE = "com.hrp.asset.mapper.AssetApprovalRecordMapper";

    @Override
    public AssetApprovalRecord selectById(Long id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }

    @Override
    public List<AssetApprovalRecord> selectByApprovalId(Long approvalId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByApprovalId", approvalId);
    }

    @Override
    public List<AssetApprovalRecord> selectByApproverId(String approverId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByApproverId", approverId);
    }

    @Override
    public int insert(AssetApprovalRecord record) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", record);
    }

    @Override
    public int updateById(AssetApprovalRecord record) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", record);
    }

    @Override
    public int deleteById(Long id) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", id);
    }
}



