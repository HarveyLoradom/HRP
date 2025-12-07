package com.hrp.efficiency.mapper;

import com.hrp.common.entity.IncomeEquipment;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

public interface IncomeEquipmentMapper {
    IncomeEquipment selectById(@Param("id") Long id);
    List<IncomeEquipment> selectByEquipmentId(@Param("equipmentId") Long equipmentId);
    List<IncomeEquipment> selectByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    List<IncomeEquipment> selectAll();
    int insert(IncomeEquipment incomeEquipment);
    int updateById(IncomeEquipment incomeEquipment);
    int deleteById(@Param("id") Long id);
}

