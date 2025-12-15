package com.hrp.asset.mapper;

import com.hrp.common.entity.AssetApproval;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AssetApprovalMapper {
    AssetApproval selectById(@Param("id") Long id);
    AssetApproval selectByApprovalNo(@Param("approvalNo") String approvalNo);
    List<AssetApproval> selectByType(@Param("approvalType") String approvalType);
    List<AssetApproval> selectByApplicant(@Param("applicantId") Long applicantId);
    List<AssetApproval> selectByApprover(@Param("approverId") String approverId);
    List<AssetApproval> selectAll();
    List<AssetApproval> selectByStatus(@Param("status") String status);
    int insert(AssetApproval assetApproval);
    int updateById(AssetApproval assetApproval);
    int deleteById(@Param("id") Long id);
}







