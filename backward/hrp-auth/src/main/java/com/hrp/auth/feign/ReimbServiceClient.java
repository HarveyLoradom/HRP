package com.hrp.auth.feign;

import com.hrp.common.entity.CtrlPayout;
import com.hrp.common.entity.CtrlPayoutDTO;
import com.hrp.common.entity.BudgetDetailRecord;
import com.hrp.common.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * Reimb服务Feign客户端
 * 用于调用reimb服务的接口
 */
@FeignClient(name = "hrp-reimb", path = "/reimb/payout")
public interface ReimbServiceClient {
    
    /**
     * 根据申请单号获取报账申请信息
     * 
     * @param payoutBillcode 申请单号
     * @return 报账申请信息
     */
    @GetMapping("/billcode/{payoutBillcode}")
    Result<CtrlPayout> getCtrlPayoutByBillcode(@PathVariable("payoutBillcode") String payoutBillcode);
    
    /**
     * 根据申请单号获取完整信息（包括发票、支付清单）
     * 
     * @param payoutBillcode 申请单号
     * @return 报账申请完整信息
     */
    @GetMapping("/billcode/{payoutBillcode}/detail")
    Result<CtrlPayoutDTO> getCtrlPayoutDTOByBillcode(@PathVariable("payoutBillcode") String payoutBillcode);
    
    /**
     * 更新报账申请信息
     * 
     * @param ctrlPayout 报账申请信息
     * @return 更新后的报账申请信息
     */
    @PutMapping
    Result<CtrlPayout> updateCtrlPayout(@RequestBody CtrlPayout ctrlPayout);
    
    /**
     * 根据业务单号获取预算明细记录
     * 
     * @param businessNo 业务单号
     * @return 预算明细记录列表
     */
    @GetMapping("/budget-details/{businessNo}")
    Result<List<BudgetDetailRecord>> getBudgetDetailsByBusinessNo(@PathVariable("businessNo") String businessNo);
    
    /**
     * 更新报账单的发票和支付清单
     * 
     * @param payoutId 报账单ID
     * @param dto 包含发票和支付清单的DTO
     * @return 更新结果
     */
    @PutMapping("/{payoutId}/invoices-payments")
    Result<Void> updateInvoicesAndPayments(@PathVariable("payoutId") Long payoutId, @RequestBody CtrlPayoutDTO dto);
}

