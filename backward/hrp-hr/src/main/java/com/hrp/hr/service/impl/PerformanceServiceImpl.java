package com.hrp.hr.service.impl;

import com.hrp.hr.mapper.PerformanceMapper;
import com.hrp.hr.service.PerformanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PerformanceServiceImpl implements PerformanceService {

    @Autowired
    private PerformanceMapper performanceMapper;

    @Override
    public List<?> getMyList(Long empId, String year, String quarter) {
        if (empId == null) {
            return List.of();
        }
        Map<String, Object> params = new HashMap<>();
        params.put("empId", empId);
        params.put("year", year);
        params.put("quarter", quarter);
        return performanceMapper.selectByEmpIdAndPeriod(params);
    }
}





