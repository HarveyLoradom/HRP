package com.hrp.reimb.feign;

import com.hrp.common.entity.Result;
import com.hrp.common.entity.BudgetDetailRecord;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * Budg服务Feign客户端
 * 用于调用budg服务的接口
 */
@FeignClient(name = "hrp-budg", path = "/budg")
public interface BudgServiceClient {
    
    /**
     * 根据业务单号查询预算明细记录
     */
    @GetMapping("/detail/business-no/{businessNo}")
    Result<List<BudgetDetailRecord>> getBudgetDetailsByBusinessNo(@PathVariable("businessNo") String businessNo);
    
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
     * 根据业务ID取消预算明细记录
     */
    @PostMapping("/detail/record/cancel-by-id/{businessId}")
    Result<Boolean> cancelBudgetDetailsByBusinessId(@PathVariable("businessId") Long businessId);
}

