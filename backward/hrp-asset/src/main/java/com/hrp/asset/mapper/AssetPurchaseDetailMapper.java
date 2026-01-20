package com.hrp.asset.mapper;

import com.hrp.common.entity.AssetPurchaseDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 采购明细表数据访问接口
 */
public interface AssetPurchaseDetailMapper {
    List<AssetPurchaseDetail> selectByPurchaseId(@Param("purchaseId") Long purchaseId);
    List<AssetPurchaseDetail> selectByOrderNo(@Param("orderNo") String orderNo);
    int insert(AssetPurchaseDetail detail);
    int insertBatch(@Param("details") List<AssetPurchaseDetail> details);
    int updateById(AssetPurchaseDetail detail);
    int deleteByPurchaseId(@Param("purchaseId") Long purchaseId);
    int deleteById(@Param("id") Long id);
}

