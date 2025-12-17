package com.hrp.asset.mapper;

import com.hrp.common.entity.AssetApprovalRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AssetApprovalRecordMapper {
    AssetApprovalRecord selectById(@Param("id") Long id);
    List<AssetApprovalRecord> selectByApprovalId(@Param("approvalId") Long approvalId);
    List<AssetApprovalRecord> selectByApproverId(@Param("approverId") String approverId);
    int insert(AssetApprovalRecord record);
    int updateById(AssetApprovalRecord record);
    int deleteById(@Param("id") Long id);
}












