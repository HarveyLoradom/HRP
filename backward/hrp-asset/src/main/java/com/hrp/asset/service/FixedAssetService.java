package com.hrp.asset.service;

import com.hrp.common.entity.FixedAsset;

import java.util.List;

public interface FixedAssetService {
    List<FixedAsset> getAll();
    List<FixedAsset> getByStatus(String status);
    FixedAsset getById(Long id);
    boolean save(FixedAsset fixedAsset);
    boolean update(FixedAsset fixedAsset);
    boolean delete(Long id);
}

