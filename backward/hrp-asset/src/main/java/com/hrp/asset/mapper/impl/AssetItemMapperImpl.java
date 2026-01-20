package com.hrp.asset.mapper.impl;

import com.hrp.asset.mapper.AssetItemMapper;
import com.hrp.common.entity.AssetItem;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class AssetItemMapperImpl implements AssetItemMapper {
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    private static final String NAMESPACE = "com.hrp.asset.mapper.AssetItemMapper";

    @Override
    public List<AssetItem> selectByConditions(String assetCode, String assetName, Long level1Id, Long level2Id, Long categoryId, Integer status) {
        Map<String, Object> params = new HashMap<>();
        if (assetCode != null && !assetCode.isEmpty()) {
            params.put("assetCode", assetCode);
        }
        if (assetName != null && !assetName.isEmpty()) {
            params.put("assetName", assetName);
        }
        if (level1Id != null) {
            params.put("level1Id", level1Id);
        }
        if (level2Id != null) {
            params.put("level2Id", level2Id);
        }
        if (categoryId != null) {
            params.put("categoryId", categoryId);
        }
        if (status != null) {
            params.put("status", status);
        }
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByConditions", params);
    }

    @Override
    public AssetItem selectById(Long id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }

    @Override
    public AssetItem selectByCode(String code) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectByCode", code);
    }

    @Override
    public int insert(AssetItem item) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", item);
    }

    @Override
    public int updateById(AssetItem item) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", item);
    }

    @Override
    public int deleteById(Long id) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", id);
    }

    @Override
    public String selectMaxCodeByCategoryId(Long categoryId) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectMaxCodeByCategoryId", categoryId);
    }
}

