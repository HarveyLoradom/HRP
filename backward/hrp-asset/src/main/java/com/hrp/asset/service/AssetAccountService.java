package com.hrp.asset.service;

import com.hrp.common.entity.AssetAccount;
import com.hrp.common.entity.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 资产台账服务接口
 */
public interface AssetAccountService {
    /**
     * 分页查询资产台账
     */
    PageResult<AssetAccount> getPage(Long page, Long size, String assetCode, String assetName, 
                                     String spec, String manufacturer, Long level1Id, Long level2Id, 
                                     Long categoryId, Boolean hasStock);
    
    /**
     * 根据资产编码查询资产台账
     */
    AssetAccount getByAssetCode(String assetCode);
    
    /**
     * 根据资产编码查询对应的入库单号、采购单号、申请单号
     */
    List<Map<String, Object>> getStorageInfoByAssetCode(String assetCode);
    
    /**
     * 根据资产编码查询对应的领用单号和领用数量
     */
    List<Map<String, Object>> getReceiveInfoByAssetCode(String assetCode);
}

