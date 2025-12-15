package com.hrp.efficiency.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface EquipmentReportMapper {
    List<?> selectAll();
    Object selectById(@Param("id") Long id);
    Object selectByReportNo(@Param("reportNo") String reportNo);
    int insert(Map<String, Object> data);
}





