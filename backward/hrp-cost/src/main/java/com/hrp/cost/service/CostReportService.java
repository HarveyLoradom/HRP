package com.hrp.cost.service;

import com.hrp.common.entity.CostReport;

import java.util.List;
import java.util.Map;

public interface CostReportService {
    List<CostReport> getAll();
    List<CostReport> getByPeriod(String reportPeriod);
    CostReport getById(Long id);
    boolean save(CostReport costReport);
    boolean update(CostReport costReport);
    boolean delete(Long id);
    CostReport generate(Map<String, Object> params);
    Map<String, Object> getChartData(Long reportId);
}

