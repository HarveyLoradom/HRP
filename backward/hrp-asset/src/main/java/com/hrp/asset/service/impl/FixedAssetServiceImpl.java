package com.hrp.asset.service.impl;

import com.hrp.asset.mapper.FixedAssetMapper;
import com.hrp.asset.service.FixedAssetService;
import com.hrp.common.entity.FixedAsset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FixedAssetServiceImpl implements FixedAssetService {

    @Autowired
    private FixedAssetMapper fixedAssetMapper;

    @Override
    public List<FixedAsset> getAll() {
        return fixedAssetMapper.selectAll();
    }

    @Override
    public List<FixedAsset> getByStatus(String status) {
        return fixedAssetMapper.selectByStatus(status);
    }

    @Override
    public FixedAsset getById(Long id) {
        return fixedAssetMapper.selectById(id);
    }

    @Override
    @Transactional
    public boolean save(FixedAsset fixedAsset) {
        if (fixedAsset.getAssetNo() == null || fixedAsset.getAssetNo().isEmpty()) {
            fixedAsset.setAssetNo("ASSET" + System.currentTimeMillis());
        }
        if (fixedAsset.getStatus() == null || fixedAsset.getStatus().isEmpty()) {
            fixedAsset.setStatus("NORMAL");
        }
        return fixedAssetMapper.insert(fixedAsset) > 0;
    }

    @Override
    @Transactional
    public boolean update(FixedAsset fixedAsset) {
        return fixedAssetMapper.updateById(fixedAsset) > 0;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        return fixedAssetMapper.deleteById(id) > 0;
    }
}

