package com.hrp.asset.service;

import com.hrp.common.entity.AssetPurchase;
import com.hrp.common.entity.AssetPurchaseDetail;

import java.util.List;

/**
 * 采购服务接口
 */
public interface AssetPurchaseService {
    // 查询方法
    AssetPurchase getById(Long id);
    AssetPurchase getByOrderNo(String orderNo);
    List<AssetPurchase> getByApplyNo(String applyNo);
    com.hrp.common.entity.PageResult<AssetPurchase> getPage(Long page, Long size, String orderNo, String applyNo, String purchaseStatus, String startDate, String endDate);
    
    // 保存和更新方法
    AssetPurchase save(AssetPurchase purchase, List<AssetPurchaseDetail> details);
    AssetPurchase update(AssetPurchase purchase, List<AssetPurchaseDetail> details);
    boolean delete(Long id);
    
    // 业务方法
    AssetPurchase completePurchase(Long purchaseId, String contractNo);
    
    // 明细相关方法
    List<AssetPurchaseDetail> getDetailsByPurchaseId(Long purchaseId);
    List<AssetPurchaseDetail> getDetailsByOrderNo(String orderNo);
}

