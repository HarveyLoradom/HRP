package com.hrp.asset.service;

import com.hrp.common.entity.AssetReceiveMain;
import com.hrp.common.entity.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 资产领用服务接口
 */
public interface AssetReceiveService {
    /**
     * 分页查询资产领用列表
     */
    PageResult<AssetReceiveMain> getPage(Long page, Long size, String receiveNo, Long deptId, 
                                         String applyEmpId, String operatorCode);
    
    /**
     * 根据ID查询资产领用主表
     */
    AssetReceiveMain getById(Long id);
    
    /**
     * 根据领用单号查询资产领用主表
     */
    AssetReceiveMain getByReceiveNo(String receiveNo);
    
    /**
     * 保存资产领用（主表和明细表）
     */
    AssetReceiveMain save(AssetReceiveMain receiveMain, java.util.List<com.hrp.common.entity.AssetReceiveDetail> details, String currentUserId);
    
    /**
     * 生成领用单号：ZCLY年月日0001
     */
    String generateReceiveNo();
}

