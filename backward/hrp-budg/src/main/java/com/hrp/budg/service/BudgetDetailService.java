package com.hrp.budg.service;

import com.hrp.common.entity.BudgetDetail;
import com.hrp.common.entity.BudgetExecutionDetail;
import com.hrp.common.entity.BudgetApplyDetail;
import com.hrp.common.entity.PageResult;

import java.util.List;

/**
 * 预算明细服务接口
 */
public interface BudgetDetailService {
    PageResult<BudgetDetail> getPage(Long page, Long size, Long deptId, String budgetYear, String categoryType, String itemName);
    List<BudgetExecutionDetail> getExecutionDetails(Long itemId, Long subjectId);
    List<BudgetApplyDetail> getApplyDetails(Long itemId, Long subjectId);
}

