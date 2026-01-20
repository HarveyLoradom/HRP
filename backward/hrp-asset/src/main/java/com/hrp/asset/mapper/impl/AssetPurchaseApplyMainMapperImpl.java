package com.hrp.asset.mapper.impl;

import com.hrp.asset.mapper.AssetPurchaseApplyMainMapper;
import com.hrp.common.entity.AssetPurchaseApplyMain;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class AssetPurchaseApplyMainMapperImpl implements AssetPurchaseApplyMainMapper {
    
    private static final String NAMESPACE = "com.hrp.asset.mapper.AssetPurchaseApplyMainMapper";
    
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    
    @Override
    public AssetPurchaseApplyMain selectById(Long id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }
    
    @Override
    public AssetPurchaseApplyMain selectByApplyNo(String applyNo) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectByApplyNo", applyNo);
    }
    
    @Override
    public List<AssetPurchaseApplyMain> selectByEmpId(Long empId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByEmpId", empId);
    }
    
    @Override
    public List<AssetPurchaseApplyMain> selectByStatus(String status) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByStatus", status);
    }
    
    @Override
    public List<AssetPurchaseApplyMain> selectByConditions(String applyNo, String applyEmpName, String status, String startDate, String endDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("applyNo", applyNo);
        params.put("applyEmpName", applyEmpName);
        params.put("status", status);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByConditions", params);
    }
    
    @Override
    public List<AssetPurchaseApplyMain> selectByApprover(String userId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByApprover", userId);
    }
    
    @Override
    public String selectMaxApplyNoByPrefix(String prefix) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectMaxApplyNoByPrefix", prefix);
    }
    
    @Override
    public int insert(AssetPurchaseApplyMain apply) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", apply);
    }
    
    @Override
    public int updateById(AssetPurchaseApplyMain apply) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", apply);
    }
    
    @Override
    public int deleteById(Long id) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", id);
    }
}

