package com.hrp.cost.service;

import com.hrp.common.entity.CostAccounting;

import java.util.List;
import java.util.Map;

public interface CostAccountingService {
    List<CostAccounting> getAll();
    List<CostAccounting> getByPeriod(String accountingPeriod);
    CostAccounting getById(Long id);
    boolean save(CostAccounting costAccounting);
    boolean update(CostAccounting costAccounting);
    boolean delete(Long id);
    Map<String, Object> getDetail(Long id);
    boolean calculate(Long accountingId);
}

