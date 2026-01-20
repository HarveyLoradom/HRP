package com.hrp.asset.mapper.impl;

import com.hrp.asset.mapper.AssetReceiveMainMapper;
import com.hrp.common.entity.AssetReceiveMain;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class AssetReceiveMainMapperImpl implements AssetReceiveMainMapper {
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    private static final String NAMESPACE = "com.hrp.asset.mapper.AssetReceiveMainMapper";

    @Override
    public int insert(AssetReceiveMain receiveMain) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", receiveMain);
    }

    @Override
    public AssetReceiveMain selectById(Long id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }

    @Override
    public AssetReceiveMain selectByReceiveNo(String receiveNo) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectByReceiveNo", receiveNo);
    }

    @Override
    public List<AssetReceiveMain> selectByConditions(String receiveNo, Long deptId, String applyEmpId, String operatorCode) {
        Map<String, Object> params = new java.util.HashMap<>();
        params.put("receiveNo", receiveNo);
        params.put("deptId", deptId);
        params.put("applyEmpId", applyEmpId);
        params.put("operatorCode", operatorCode);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByConditions", params);
    }

    @Override
    public String selectMaxReceiveNoByPrefix(String prefix) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectMaxReceiveNoByPrefix", prefix);
    }
}

