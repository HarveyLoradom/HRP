package com.hrp.cost.mapper;

import com.hrp.common.entity.CostAccounting;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CostAccountingMapper {
    CostAccounting selectById(@Param("id") Long id);
    List<CostAccounting> selectByPeriod(@Param("accountingPeriod") String accountingPeriod);
    List<CostAccounting> selectAll();
    int insert(CostAccounting costAccounting);
    int updateById(CostAccounting costAccounting);
    int deleteById(@Param("id") Long id);
}

