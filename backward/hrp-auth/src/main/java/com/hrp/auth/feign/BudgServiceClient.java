package com.hrp.auth.feign;

import com.hrp.common.entity.BudgetApply;
import com.hrp.common.entity.BudgetSubject;
import com.hrp.common.entity.BudgetDetailRecord;
import com.hrp.common.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * Budg服务Feign客户端
 * 用于调用budg服务的接口
 */
@FeignClient(name = "hrp-budg", path = "/budg")
public interface BudgServiceClient {
    
    /**
     * 根据申请单号获取预算申请信息
     * 
     * @param applyNo 申请单号
     * @return 预算申请信息
     */
    @GetMapping("/apply/no/{applyNo}")
    Result<BudgetApply> getBudgetApplyByNo(@PathVariable("applyNo") String applyNo);
    
    /**
     * 根据预算主体ID获取预算主体信息
     * 
     * @param subjectId 预算主体ID
     * @return 预算主体信息
     */
    @GetMapping("/subject/{subjectId}")
    Result<BudgetSubject> getBudgetSubjectById(@PathVariable("subjectId") Long subjectId);
    
    /**
     * 更新预算申请信息
     * 
     * @param budgetApply 预算申请信息
     * @return 更新后的预算申请信息
     */
    @PutMapping("/apply")
    Result<BudgetApply> updateBudgetApply(@RequestBody BudgetApply budgetApply);
    
    /**
     * 批量保存预算明细记录
     */
    @PostMapping("/detail/record/batch")
    Result<Boolean> saveBudgetDetailRecords(@RequestBody List<BudgetDetailRecord> records);
    
    /**
     * 根据业务单号取消预算明细记录
     */
    @PostMapping("/detail/record/cancel/{businessNo}")
    Result<Boolean> cancelBudgetDetailsByBusinessNo(@PathVariable("businessNo") String businessNo);
    
    /**
     * 批量更新预算明细记录（根据detailId更新）
     */
    @PutMapping("/detail/record/batch")
    Result<Boolean> updateBudgetDetailRecords(@RequestBody List<BudgetDetailRecord> records);
}

