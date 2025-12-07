package com.hrp.cost.mapper;

import com.hrp.common.entity.CostReport;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CostReportMapper {
    CostReport selectById(@Param("id") Long id);
    List<CostReport> selectByPeriod(@Param("reportPeriod") String reportPeriod);
    List<CostReport> selectAll();
    int insert(CostReport costReport);
    int updateById(CostReport costReport);
    int deleteById(@Param("id") Long id);
}

