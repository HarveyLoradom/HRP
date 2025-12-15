package com.hrp.efficiency.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface InvestmentMapper {
    List<?> selectAll();
    Object selectById(@Param("id") Long id);
    int insert(Map<String, Object> data);
    int updateById(Map<String, Object> data);
}





