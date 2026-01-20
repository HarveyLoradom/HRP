package com.hrp.asset.mapper.impl;

import com.hrp.asset.mapper.AssetAccountMapper;
import com.hrp.common.entity.AssetAccount;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class AssetAccountMapperImpl implements AssetAccountMapper {
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    private static final String NAMESPACE = "com.hrp.asset.mapper.AssetAccountMapper";

    @Override
    public AssetAccount selectByAssetCode(String assetCode) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectByAssetCode", assetCode);
    }

    @Override
    public int insert(AssetAccount account) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", account);
    }

    @Override
    public int updateByAssetCode(AssetAccount account) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateByAssetCode", account);
    }

    @Override
    public int deleteByAssetCode(String assetCode) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteByAssetCode", assetCode);
    }
    
    @Override
    public java.util.List<AssetAccount> selectByConditions(String assetCode, String assetName, String spec, 
                                                           String manufacturer, Long level1Id, Long level2Id, 
                                                           Long categoryId, Boolean hasStock) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("assetCode", assetCode);
        params.put("assetName", assetName);
        params.put("spec", spec);
        params.put("manufacturer", manufacturer);
        params.put("level1Id", level1Id);
        params.put("level2Id", level2Id);
        params.put("categoryId", categoryId);
        params.put("hasStock", hasStock);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByConditions", params);
    }
    
    @Override
    public java.util.List<java.util.Map<String, Object>> selectStorageInfoByAssetCode(String assetCode) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectStorageInfoByAssetCode", assetCode);
    }
    
    @Override
    public java.util.List<java.util.Map<String, Object>> selectReceiveInfoByAssetCode(String assetCode) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectReceiveInfoByAssetCode", assetCode);
    }
}

