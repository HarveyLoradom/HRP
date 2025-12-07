package com.hrp.efficiency.mapper;

import com.hrp.common.entity.Equipment;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface EquipmentMapper {
    Equipment selectById(@Param("id") Long id);
    Equipment selectByEquipmentCode(@Param("equipmentCode") String equipmentCode);
    List<Equipment> selectByStatus(@Param("status") String status);
    List<Equipment> selectAll();
    int insert(Equipment equipment);
    int updateById(Equipment equipment);
    int deleteById(@Param("id") Long id);
}

