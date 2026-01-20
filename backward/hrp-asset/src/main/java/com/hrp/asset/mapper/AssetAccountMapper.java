package com.hrp.asset.mapper;

import com.hrp.common.entity.AssetAccount;
import org.apache.ibatis.annotations.Param;

/**
 * 资产台账数据访问接口
 */
public interface AssetAccountMapper {
    /**
     * 根据资产编码查询资产台账
     */
    AssetAccount selectByAssetCode(@Param("assetCode") String assetCode);
    
    /**
     * 插入资产台账
     */
    int insert(AssetAccount account);
    
    /**
     * 更新资产台账
     */
    int updateByAssetCode(AssetAccount account);
    
    /**
     * 根据资产编码删除资产台账
     */
    int deleteByAssetCode(@Param("assetCode") String assetCode);
    
    /**
     * 分页查询资产台账（支持多条件查询）
     */
    java.util.List<AssetAccount> selectByConditions(
        @Param("assetCode") String assetCode,
        @Param("assetName") String assetName,
        @Param("spec") String spec,
        @Param("manufacturer") String manufacturer,
        @Param("level1Id") Long level1Id,
        @Param("level2Id") Long level2Id,
        @Param("categoryId") Long categoryId,
        @Param("hasStock") Boolean hasStock // true-有库存，false-无库存，null-全部
    );
    
    /**
     * 根据资产编码查询对应的入库单号、采购单号、申请单号（用于点击数量查看）
     */
    java.util.List<java.util.Map<String, Object>> selectStorageInfoByAssetCode(@Param("assetCode") String assetCode);
    
    /**
     * 根据资产编码查询对应的领用单号和领用数量（用于点击数量查看出库信息）
     */
    java.util.List<java.util.Map<String, Object>> selectReceiveInfoByAssetCode(@Param("assetCode") String assetCode);
}

