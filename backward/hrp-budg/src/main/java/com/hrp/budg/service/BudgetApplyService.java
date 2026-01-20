package com.hrp.budg.service;

import com.hrp.common.entity.BudgetApply;
import com.hrp.common.entity.PageResult;

import java.util.List;

/**
 * 预算申请服务接口
 */
public interface BudgetApplyService {
    BudgetApply getById(Long id);
    BudgetApply getByNo(String applyNo);
    PageResult<BudgetApply> getPage(Long page, Long size, String applyNo, Long itemId, String applicantName, String applicantCode, String status, String startDate, String endDate);
    PageResult<BudgetApply> getPageByApprover(Long page, Long size, String currentUserId, String applyNo, Long itemId, String applicantName, String status, String startDate, String endDate);
    List<BudgetApply> getByItemId(Long itemId);
    BudgetApply save(BudgetApply budgetApply);
    BudgetApply update(BudgetApply budgetApply);
    boolean submit(Long id);
    boolean approve(Long id, String opinion, String approverSignature);
    boolean reject(Long id, String opinion);
    boolean returnApply(Long id, String returnType, String opinion);
    boolean withdraw(Long id);
    boolean delete(Long id);
}

