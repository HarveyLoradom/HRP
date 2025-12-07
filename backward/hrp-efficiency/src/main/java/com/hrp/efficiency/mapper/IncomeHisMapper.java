package com.hrp.efficiency.mapper;

import com.hrp.common.entity.IncomeHis;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

public interface IncomeHisMapper {
    IncomeHis selectById(@Param("id") Long id);
    List<IncomeHis> selectByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    List<IncomeHis> selectAll();
    int insert(IncomeHis incomeHis);
    int updateById(IncomeHis incomeHis);
    int deleteById(@Param("id") Long id);
}

