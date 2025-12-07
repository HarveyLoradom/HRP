package com.hrp.efficiency.service;

import com.hrp.common.entity.IncomeHis;

import java.time.LocalDate;
import java.util.List;

public interface IncomeHisService {
    List<IncomeHis> getAll();
    List<IncomeHis> getByDateRange(LocalDate startDate, LocalDate endDate);
    IncomeHis getById(Long id);
    boolean save(IncomeHis incomeHis);
    boolean update(IncomeHis incomeHis);
    boolean delete(Long id);
}

