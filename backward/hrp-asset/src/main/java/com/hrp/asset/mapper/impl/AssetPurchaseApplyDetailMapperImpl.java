package com.hrp.asset.mapper.impl;

import com.hrp.asset.mapper.AssetPurchaseApplyDetailMapper;
import com.hrp.common.entity.AssetPurchaseApplyDetail;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AssetPurchaseApplyDetailMapperImpl implements AssetPurchaseApplyDetailMapper {
    
    private static final String NAMESPACE = "com.hrp.asset.mapper.AssetPurchaseApplyDetailMapper";
    
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    
    @Override
    public AssetPurchaseApplyDetail selectById(Long id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }
    
    @Override
    public List<AssetPurchaseApplyDetail> selectByApplyId(Long applyId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByApplyId", applyId);
    }
    
    @Override
    public int insert(AssetPurchaseApplyDetail detail) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", detail);
    }
    
    @Override
    public int insertBatch(List<AssetPurchaseApplyDetail> details) {
        if (details == null || details.isEmpty()) {
            return 0;
        }
        // MyBatis处理List参数时，默认参数名是"list"，所以XML中使用collection="list"
        return sqlSessionTemplate.insert(NAMESPACE + ".insertBatch", details);
    }
    
    @Override
    public int updateById(AssetPurchaseApplyDetail detail) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", detail);
    }
    
    @Override
    public int deleteByApplyId(Long applyId) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteByApplyId", applyId);
    }
    
    @Override
    public int deleteById(Long id) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", id);
    }
}

