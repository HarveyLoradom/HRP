package com.hrp.efficiency.mapper;

import com.hrp.common.entity.CostData;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

public interface CostDataMapper {
    CostData selectById(@Param("id") Long id);
    List<CostData> selectByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    List<CostData> selectAll();
    int insert(CostData costData);
    int updateById(CostData costData);
    int deleteById(@Param("id") Long id);
}

