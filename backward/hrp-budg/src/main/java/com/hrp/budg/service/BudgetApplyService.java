package com.hrp.budg.service;

import com.hrp.common.entity.BudgetApply;
import com.hrp.common.entity.PageResult;

/**
 * 预算申请服务接口
 */
public interface BudgetApplyService {
    BudgetApply getById(Long id);
    PageResult<BudgetApply> getPage(Long page, Long size);
    boolean save(BudgetApply budgetApply);
    boolean update(BudgetApply budgetApply);
    boolean submit(Long id);
    boolean approve(Long id, String opinion);
    boolean reject(Long id, String opinion);
}

