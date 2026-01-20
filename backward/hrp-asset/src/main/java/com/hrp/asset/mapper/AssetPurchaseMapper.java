package com.hrp.asset.mapper;

import com.hrp.common.entity.AssetPurchase;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 采购主表数据访问接口
 */
public interface AssetPurchaseMapper {
    AssetPurchase selectById(@Param("id") Long id);
    AssetPurchase selectByOrderNo(@Param("orderNo") String orderNo);
    List<AssetPurchase> selectByApplyNo(@Param("applyNo") String applyNo);
    List<AssetPurchase> selectByConditions(@Param("orderNo") String orderNo,
                                            @Param("applyNo") String applyNo,
                                            @Param("purchaseStatus") String purchaseStatus,
                                            @Param("startDate") String startDate,
                                            @Param("endDate") String endDate);
    String selectMaxOrderNoByPrefix(@Param("prefix") String prefix);
    int insert(AssetPurchase purchase);
    int updateById(AssetPurchase purchase);
    int deleteById(@Param("id") Long id);
}

