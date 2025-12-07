package com.hrp.cost.service;

import com.hrp.common.entity.CostAnalysis;

import java.util.List;

public interface CostAnalysisService {
    List<CostAnalysis> getAll();
    CostAnalysis getById(Long id);
    boolean save(CostAnalysis costAnalysis);
    boolean update(CostAnalysis costAnalysis);
    boolean delete(Long id);
}

