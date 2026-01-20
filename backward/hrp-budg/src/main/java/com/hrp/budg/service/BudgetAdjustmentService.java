package com.hrp.budg.service;

import com.hrp.common.entity.BudgetAdjustment;
import com.hrp.common.entity.PageResult;

import java.util.Map;

/**
 * 预算调整服务接口
 */
public interface BudgetAdjustmentService {
    BudgetAdjustment getById(Long id);
    BudgetAdjustment getByNo(String no);
    PageResult<BudgetAdjustment> getPage(Long page, Long size, Map<String, Object> params);
    boolean save(BudgetAdjustment budgetAdjustment);
    boolean update(BudgetAdjustment budgetAdjustment);
    boolean delete(Long id);
    boolean submit(BudgetAdjustment budgetAdjustment);
    /**
     * 保存并提交（在同一个事务中完成保存和提交操作）
     */
    boolean saveAndSubmit(BudgetAdjustment budgetAdjustment);
}

