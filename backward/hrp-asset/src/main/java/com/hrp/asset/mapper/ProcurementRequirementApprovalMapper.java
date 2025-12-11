package com.hrp.asset.mapper;

import com.hrp.common.entity.ProcurementRequirementApproval;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProcurementRequirementApprovalMapper {
    ProcurementRequirementApproval selectById(@Param("id") Long id);
    List<ProcurementRequirementApproval> selectByRequirementId(@Param("requirementId") Long requirementId);
    int insert(ProcurementRequirementApproval approval);
}
