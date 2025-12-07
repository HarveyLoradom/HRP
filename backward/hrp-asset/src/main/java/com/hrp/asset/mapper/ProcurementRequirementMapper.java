package com.hrp.asset.mapper;

import com.hrp.common.entity.ProcurementRequirement;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProcurementRequirementMapper {
    ProcurementRequirement selectById(@Param("id") Long id);
    List<ProcurementRequirement> selectByStatus(@Param("status") String status);
    List<ProcurementRequirement> selectAll();
    int insert(ProcurementRequirement requirement);
    int updateById(ProcurementRequirement requirement);
    int deleteById(@Param("id") Long id);
}

