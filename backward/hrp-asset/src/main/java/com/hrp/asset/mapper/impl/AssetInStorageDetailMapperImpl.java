package com.hrp.asset.mapper.impl;

import com.hrp.asset.mapper.AssetInStorageDetailMapper;
import com.hrp.common.entity.AssetInStorageDetail;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AssetInStorageDetailMapperImpl implements AssetInStorageDetailMapper {
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    private static final String NAMESPACE = "com.hrp.asset.mapper.AssetInStorageDetailMapper";

    @Override
    public List<AssetInStorageDetail> selectByStorageId(Long storageId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByStorageId", storageId);
    }

    @Override
    public List<AssetInStorageDetail> selectByStorageNo(String storageNo) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByStorageNo", storageNo);
    }

    @Override
    public int insert(AssetInStorageDetail detail) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", detail);
    }

    @Override
    public int insertBatch(List<AssetInStorageDetail> details) {
        if (details == null || details.isEmpty()) {
            return 0;
        }
        return sqlSessionTemplate.insert(NAMESPACE + ".insertBatch", details);
    }

    @Override
    public int updateById(AssetInStorageDetail detail) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", detail);
    }

    @Override
    public int deleteByStorageId(Long storageId) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteByStorageId", storageId);
    }

    @Override
    public int deleteById(Long id) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", id);
    }
}

