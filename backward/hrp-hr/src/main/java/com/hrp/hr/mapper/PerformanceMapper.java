package com.hrp.hr.mapper;

import java.util.List;
import java.util.Map;

public interface PerformanceMapper {
    List<?> selectByEmpIdAndPeriod(Map<String, Object> params);
}





