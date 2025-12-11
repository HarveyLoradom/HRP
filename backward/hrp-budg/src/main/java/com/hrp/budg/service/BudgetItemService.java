package com.hrp.budg.service;

import com.hrp.common.entity.BudgetItem;

import java.util.List;

/**
 * 预算项目服务接口
 */
public interface BudgetItemService {
    BudgetItem getById(Long id);
    BudgetItem getByCode(String code);
    List<BudgetItem> getAll();
    List<BudgetItem> getByCategoryId(Long categoryId);
    boolean save(BudgetItem budgetItem);
    boolean update(BudgetItem budgetItem);
    boolean delete(Long id);
}




