package com.hrp.efficiency.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface DataCollectionMapper {
    int insert(Map<String, Object> data);
    List<?> selectByType(@Param("collectionType") String collectionType);
}





