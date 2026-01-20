package com.hrp.budg.service;

import com.hrp.common.entity.Budget;

import java.math.BigDecimal;
import java.util.List;

/**
 * 预算服务接口
 */
public interface BudgetService {
    Budget getById(Long id);
    Budget getByNo(String no);
    List<Budget> getAll();
    List<Budget> getByYear(String year);
    List<Budget> getBySubjectId(Long subjectId);
    List<Budget> getByItemId(Long itemId);
    List<Budget> getBySubjectAndItem(Long subjectId, Long itemId);
    boolean save(Budget budget);
    boolean update(Budget budget);
    boolean delete(Long id);
    boolean checkBudgetAmount(Long budgetId, BigDecimal amount);
    boolean executeBudget(Long budgetId, BigDecimal amount);
    /**
     * 计算预算的剩余可执行金额
     * @param budgetId 预算ID
     * @return 剩余可执行金额（预算总额 - 已执行金额）
     */
    BigDecimal getRemainingAmount(Long budgetId);
}













