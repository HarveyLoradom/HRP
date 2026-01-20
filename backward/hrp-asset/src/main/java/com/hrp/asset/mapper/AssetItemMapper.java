package com.hrp.asset.mapper;

import com.hrp.common.entity.AssetItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 资产信息数据访问接口
 */
public interface AssetItemMapper {
    /**
     * 根据条件查询资产信息列表
     */
    List<AssetItem> selectByConditions(@Param("assetCode") String assetCode,
                                       @Param("assetName") String assetName,
                                       @Param("level1Id") Long level1Id,
                                       @Param("level2Id") Long level2Id,
                                       @Param("categoryId") Long categoryId,
                                       @Param("status") Integer status);
    
    /**
     * 根据ID查询资产信息
     */
    AssetItem selectById(@Param("id") Long id);
    
    /**
     * 根据编码查询资产信息
     */
    AssetItem selectByCode(@Param("code") String code);
    
    /**
     * 插入资产信息
     */
    int insert(AssetItem item);
    
    /**
     * 更新资产信息
     */
    int updateById(AssetItem item);
    
    /**
     * 删除资产信息
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 查询指定分类下最大的编码（用于自动生成编码）
     */
    String selectMaxCodeByCategoryId(@Param("categoryId") Long categoryId);
}

