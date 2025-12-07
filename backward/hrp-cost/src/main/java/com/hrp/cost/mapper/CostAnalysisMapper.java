package com.hrp.cost.mapper;

import com.hrp.common.entity.CostAnalysis;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CostAnalysisMapper {
    CostAnalysis selectById(@Param("id") Long id);
    List<CostAnalysis> selectAll();
    int insert(CostAnalysis costAnalysis);
    int updateById(CostAnalysis costAnalysis);
    int deleteById(@Param("id") Long id);
}

