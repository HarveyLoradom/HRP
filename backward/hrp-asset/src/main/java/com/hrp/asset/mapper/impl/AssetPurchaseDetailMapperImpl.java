package com.hrp.asset.mapper.impl;

import com.hrp.asset.mapper.AssetPurchaseDetailMapper;
import com.hrp.common.entity.AssetPurchaseDetail;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AssetPurchaseDetailMapperImpl implements AssetPurchaseDetailMapper {
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    private static final String NAMESPACE = "com.hrp.asset.mapper.AssetPurchaseDetailMapper";

    @Override
    public List<AssetPurchaseDetail> selectByPurchaseId(Long purchaseId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByPurchaseId", purchaseId);
    }

    @Override
    public List<AssetPurchaseDetail> selectByOrderNo(String orderNo) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByOrderNo", orderNo);
    }

    @Override
    public int insert(AssetPurchaseDetail detail) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", detail);
    }

    @Override
    public int insertBatch(List<AssetPurchaseDetail> details) {
        if (details == null || details.isEmpty()) {
            return 0;
        }
        return sqlSessionTemplate.insert(NAMESPACE + ".insertBatch", details);
    }

    @Override
    public int updateById(AssetPurchaseDetail detail) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", detail);
    }

    @Override
    public int deleteByPurchaseId(Long purchaseId) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteByPurchaseId", purchaseId);
    }

    @Override
    public int deleteById(Long id) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", id);
    }
}

