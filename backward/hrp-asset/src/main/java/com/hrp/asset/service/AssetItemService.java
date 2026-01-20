package com.hrp.asset.service;

import com.hrp.common.entity.AssetItem;
import com.hrp.common.entity.PageResult;
import com.hrp.common.entity.Result;

import java.util.List;

/**
 * 资产信息维护服务接口
 */
public interface AssetItemService {
    /**
     * 分页查询资产信息
     */
    PageResult<AssetItem> getPage(Long page, Long size, String assetCode, String assetName, Long level1Id, Long level2Id, Long categoryId, Integer status);
    
    /**
     * 查询资产信息列表
     */
    List<AssetItem> getList(String assetCode, String assetName, Long level1Id, Long level2Id, Long categoryId, Integer status);
    
    /**
     * 根据ID查询资产信息
     */
    AssetItem getById(Long id);
    
    /**
     * 新增资产信息
     */
    boolean save(AssetItem item);
    
    /**
     * 更新资产信息
     */
    boolean update(AssetItem item);
    
    /**
     * 删除资产信息（物理删除）
     */
    boolean delete(Long id);
    
    /**
     * 停用资产信息
     */
    boolean stop(Long id);
    
    /**
     * 启用资产信息
     */
    boolean start(Long id);
    
    /**
     * 生成资产编码
     * 规则：根据分类ID生成唯一编码
     */
    String generateAssetCode(Long categoryId);
    
    /**
     * 批量导入资产信息
     */
    Result<String> importAssetItems(List<List<String>> dataList, String createUser);
}

