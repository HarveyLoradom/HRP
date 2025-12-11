package com.hrp.asset.mapper;

import com.hrp.common.entity.ProcurementRequirementDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProcurementRequirementDetailMapper {
    ProcurementRequirementDetail selectById(@Param("id") Long id);
    List<ProcurementRequirementDetail> selectByRequirementId(@Param("requirementId") Long requirementId);
    int insert(ProcurementRequirementDetail detail);
    int updateById(ProcurementRequirementDetail detail);
    int deleteById(@Param("id") Long id);
    int deleteByRequirementId(@Param("requirementId") Long requirementId);
}

