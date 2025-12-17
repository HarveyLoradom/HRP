package com.hrp.asset.mapper;

import com.hrp.common.entity.AssetApprovalDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AssetApprovalDetailMapper {
    AssetApprovalDetail selectById(@Param("id") Long id);
    List<AssetApprovalDetail> selectByApprovalId(@Param("approvalId") Long approvalId);
    int insert(AssetApprovalDetail detail);
    int updateById(AssetApprovalDetail detail);
    int deleteById(@Param("id") Long id);
    int deleteByApprovalId(@Param("approvalId") Long approvalId);
}












