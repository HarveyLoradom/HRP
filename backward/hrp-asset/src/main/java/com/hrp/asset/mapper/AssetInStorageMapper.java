package com.hrp.asset.mapper;

import com.hrp.common.entity.AssetInStorage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 入库主表数据访问接口
 */
public interface AssetInStorageMapper {
    AssetInStorage selectById(@Param("id") Long id);
    AssetInStorage selectByStorageNo(@Param("storageNo") String storageNo);
    List<AssetInStorage> selectByPurchaseId(@Param("purchaseId") Long purchaseId);
    List<AssetInStorage> selectByOrderNo(@Param("orderNo") String orderNo);
    List<AssetInStorage> selectByConditions(@Param("storageNo") String storageNo,
                                             @Param("orderNo") String orderNo,
                                             @Param("applyNo") String applyNo,
                                             @Param("storageStatus") String storageStatus,
                                             @Param("startDate") String startDate,
                                             @Param("endDate") String endDate);
    String selectMaxStorageNoByPrefix(@Param("prefix") String prefix);
    int insert(AssetInStorage storage);
    int updateById(AssetInStorage storage);
    int deleteById(@Param("id") Long id);
}

