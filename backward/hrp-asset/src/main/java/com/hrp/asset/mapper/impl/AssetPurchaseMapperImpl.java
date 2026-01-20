package com.hrp.asset.mapper.impl;

import com.hrp.asset.mapper.AssetPurchaseMapper;
import com.hrp.common.entity.AssetPurchase;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class AssetPurchaseMapperImpl implements AssetPurchaseMapper {
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    private static final String NAMESPACE = "com.hrp.asset.mapper.AssetPurchaseMapper";

    @Override
    public AssetPurchase selectById(Long id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }

    @Override
    public AssetPurchase selectByOrderNo(String orderNo) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectByOrderNo", orderNo);
    }

    @Override
    public List<AssetPurchase> selectByApplyNo(String applyNo) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByApplyNo", applyNo);
    }

    @Override
    public List<AssetPurchase> selectByConditions(String orderNo, String applyNo, String purchaseStatus, String startDate, String endDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderNo", orderNo);
        params.put("applyNo", applyNo);
        params.put("purchaseStatus", purchaseStatus);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByConditions", params);
    }

    @Override
    public String selectMaxOrderNoByPrefix(String prefix) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectMaxOrderNoByPrefix", prefix);
    }

    @Override
    public int insert(AssetPurchase purchase) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", purchase);
    }

    @Override
    public int updateById(AssetPurchase purchase) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", purchase);
    }

    @Override
    public int deleteById(Long id) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", id);
    }
}

