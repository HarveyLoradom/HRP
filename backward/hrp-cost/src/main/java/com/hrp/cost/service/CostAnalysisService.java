package com.hrp.cost.service;

import com.hrp.common.entity.CostAnalysis;

import java.util.List;
import java.util.Map;

public interface CostAnalysisService {
    List<CostAnalysis> getAll();
    CostAnalysis getById(Long id);
    boolean save(CostAnalysis costAnalysis);
    boolean update(CostAnalysis costAnalysis);
    boolean delete(Long id);
    Map<String, Object> getDetail(Long id);
    List<Map<String, Object>> compare(String period1, String period2);
}

