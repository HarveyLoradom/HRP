package com.hrp.asset.service;

import com.hrp.common.entity.AssetCategory;
import com.hrp.common.entity.PageResult;

import java.util.List;

/**
 * 资产分类服务接口
 */
public interface AssetCategoryService {
    PageResult<AssetCategory> getLevel1Page(Long page, Long size, Integer status, String categoryName);
    PageResult<AssetCategory> getLevel2Page(Long page, Long size, Long parentId, Integer status, String categoryName);
    PageResult<AssetCategory> getLevel3Page(Long page, Long size, Long parentId, Long level1Id, Integer status, String categoryName);
    List<AssetCategory> getLevel1List(Integer status);
    List<AssetCategory> getLevel2List(Long parentId, Integer status);
    List<AssetCategory> getLevel3List(Long parentId, Integer status);
    AssetCategory getById(Long id);
    boolean save(AssetCategory category);
    boolean update(AssetCategory category);
    boolean delete(Long id);
    boolean stop(Long id);
    boolean start(Long id);
    /**
     * 生成二级分类编码
     * 规则：一级编码+001,002...
     */
    String generateLevel2Code(Long parentId);
    
    /**
     * 生成三级分类编码
     * 规则：二级编码+001,002...
     */
    String generateLevel3Code(Long parentId);
}

