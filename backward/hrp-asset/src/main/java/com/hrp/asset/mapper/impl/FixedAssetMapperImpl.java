package com.hrp.asset.mapper.impl;

import com.hrp.asset.mapper.FixedAssetMapper;
import com.hrp.common.entity.FixedAsset;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FixedAssetMapperImpl implements FixedAssetMapper {
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    private static final String NAMESPACE = "com.hrp.asset.mapper.FixedAssetMapper";

    @Override
    public FixedAsset selectById(Long id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }

    @Override
    public FixedAsset selectByAssetNo(String assetNo) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectByAssetNo", assetNo);
    }

    @Override
    public List<FixedAsset> selectByStatus(String status) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByStatus", status);
    }

    @Override
    public List<FixedAsset> selectAll() {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAll");
    }

    @Override
    public int insert(FixedAsset fixedAsset) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", fixedAsset);
    }

    @Override
    public int updateById(FixedAsset fixedAsset) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", fixedAsset);
    }

    @Override
    public int deleteById(Long id) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", id);
    }
}

