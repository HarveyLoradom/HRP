package com.hrp.efficiency.service;

import com.hrp.common.entity.CostData;

import java.time.LocalDate;
import java.util.List;

public interface CostDataService {
    List<CostData> getAll();
    List<CostData> getByDateRange(LocalDate startDate, LocalDate endDate);
    CostData getById(Long id);
    boolean save(CostData costData);
    boolean update(CostData costData);
    boolean delete(Long id);
}

