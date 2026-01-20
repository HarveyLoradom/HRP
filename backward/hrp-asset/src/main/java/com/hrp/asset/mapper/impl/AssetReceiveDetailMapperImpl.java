package com.hrp.asset.mapper.impl;

import com.hrp.asset.mapper.AssetReceiveDetailMapper;
import com.hrp.common.entity.AssetReceiveDetail;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AssetReceiveDetailMapperImpl implements AssetReceiveDetailMapper {
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    private static final String NAMESPACE = "com.hrp.asset.mapper.AssetReceiveDetailMapper";

    @Override
    public int insert(AssetReceiveDetail detail) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", detail);
    }

    @Override
    public int insertBatch(List<AssetReceiveDetail> details) {
        if (details == null || details.isEmpty()) {
            return 0;
        }
        return sqlSessionTemplate.insert(NAMESPACE + ".insertBatch", details);
    }

    @Override
    public List<AssetReceiveDetail> selectByReceiveId(Long receiveId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByReceiveId", receiveId);
    }

    @Override
    public List<AssetReceiveDetail> selectByReceiveNo(String receiveNo) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByReceiveNo", receiveNo);
    }

    @Override
    public AssetReceiveDetail selectById(Long id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }

    @Override
    public int deleteByReceiveId(Long receiveId) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteByReceiveId", receiveId);
    }
}

