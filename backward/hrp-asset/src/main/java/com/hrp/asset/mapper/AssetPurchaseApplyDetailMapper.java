package com.hrp.asset.mapper;

import com.hrp.common.entity.AssetPurchaseApplyDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 采购申请明细表数据访问接口
 */
public interface AssetPurchaseApplyDetailMapper {
    AssetPurchaseApplyDetail selectById(@Param("id") Long id);
    List<AssetPurchaseApplyDetail> selectByApplyId(@Param("applyId") Long applyId);
    int insert(AssetPurchaseApplyDetail detail);
    int insertBatch(@Param("details") List<AssetPurchaseApplyDetail> details);
    int updateById(AssetPurchaseApplyDetail detail);
    int deleteByApplyId(@Param("applyId") Long applyId);
    int deleteById(@Param("id") Long id);
}

