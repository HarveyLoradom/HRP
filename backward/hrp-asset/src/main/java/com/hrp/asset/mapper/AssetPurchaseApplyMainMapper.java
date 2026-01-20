package com.hrp.asset.mapper;

import com.hrp.common.entity.AssetPurchaseApplyMain;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 采购申请主表数据访问接口
 */
public interface AssetPurchaseApplyMainMapper {
    AssetPurchaseApplyMain selectById(@Param("id") Long id);
    AssetPurchaseApplyMain selectByApplyNo(@Param("applyNo") String applyNo);
    List<AssetPurchaseApplyMain> selectByEmpId(@Param("empId") Long empId);
    List<AssetPurchaseApplyMain> selectByStatus(@Param("status") String status);
    List<AssetPurchaseApplyMain> selectByConditions(@Param("applyNo") String applyNo,
                                                    @Param("applyEmpName") String applyEmpName,
                                                    @Param("status") String status,
                                                    @Param("startDate") String startDate,
                                                    @Param("endDate") String endDate);
    List<AssetPurchaseApplyMain> selectByApprover(@Param("userId") String userId);
    String selectMaxApplyNoByPrefix(@Param("prefix") String prefix);
    int insert(AssetPurchaseApplyMain apply);
    int updateById(AssetPurchaseApplyMain apply);
    int deleteById(@Param("id") Long id);
}

