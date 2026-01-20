package com.hrp.asset.service;

import com.hrp.common.entity.AssetInStorage;
import com.hrp.common.entity.AssetInStorageDetail;

import java.util.List;

/**
 * 入库服务接口
 */
public interface AssetInStorageService {
    // 查询方法
    AssetInStorage getById(Long id);
    AssetInStorage getByStorageNo(String storageNo);
    List<AssetInStorage> getByPurchaseId(Long purchaseId);
    List<AssetInStorage> getByOrderNo(String orderNo);
    com.hrp.common.entity.PageResult<AssetInStorage> getPage(Long page, Long size, String storageNo, String orderNo, String applyNo, String storageStatus, String startDate, String endDate);
    
    // 保存和更新方法
    AssetInStorage save(AssetInStorage storage, List<AssetInStorageDetail> details);
    AssetInStorage update(AssetInStorage storage, List<AssetInStorageDetail> details);
    boolean delete(Long id);
    
    // 业务方法
    AssetInStorage completeStorage(Long storageId);
    
    // 明细相关方法
    List<AssetInStorageDetail> getDetailsByStorageId(Long storageId);
    List<AssetInStorageDetail> getDetailsByStorageNo(String storageNo);
}

