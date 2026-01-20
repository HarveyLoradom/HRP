package com.hrp.asset.mapper.impl;

import com.hrp.asset.mapper.AssetInStorageMapper;
import com.hrp.common.entity.AssetInStorage;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class AssetInStorageMapperImpl implements AssetInStorageMapper {
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    private static final String NAMESPACE = "com.hrp.asset.mapper.AssetInStorageMapper";

    @Override
    public AssetInStorage selectById(Long id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }

    @Override
    public AssetInStorage selectByStorageNo(String storageNo) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectByStorageNo", storageNo);
    }

    @Override
    public List<AssetInStorage> selectByPurchaseId(Long purchaseId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByPurchaseId", purchaseId);
    }

    @Override
    public List<AssetInStorage> selectByOrderNo(String orderNo) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByOrderNo", orderNo);
    }

    @Override
    public List<AssetInStorage> selectByConditions(String storageNo, String orderNo, String applyNo, String storageStatus, String startDate, String endDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("storageNo", storageNo);
        params.put("orderNo", orderNo);
        params.put("applyNo", applyNo);
        params.put("storageStatus", storageStatus);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByConditions", params);
    }

    @Override
    public String selectMaxStorageNoByPrefix(String prefix) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectMaxStorageNoByPrefix", prefix);
    }

    @Override
    public int insert(AssetInStorage storage) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", storage);
    }

    @Override
    public int updateById(AssetInStorage storage) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", storage);
    }

    @Override
    public int deleteById(Long id) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", id);
    }
}

