package com.hrp.budg.service;

import com.hrp.common.entity.BudgetExecution;

import java.util.List;

/**
 * 预算执行单服务接口
 */
public interface BudgetExecutionService {
    BudgetExecution getById(Long id);
    BudgetExecution getByNo(String no);
    List<BudgetExecution> getAll();
    List<BudgetExecution> getByBudgetId(Long budgetId);
    List<BudgetExecution> getByBusinessId(String businessType, Long businessId);
    boolean save(BudgetExecution budgetExecution);
    boolean update(BudgetExecution budgetExecution);
    boolean delete(Long id);
    boolean createExecutionFromBusiness(String businessType, Long businessId, Long budgetId, java.math.BigDecimal amount);
}













