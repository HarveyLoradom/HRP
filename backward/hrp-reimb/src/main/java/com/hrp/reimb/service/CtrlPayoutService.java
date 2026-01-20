package com.hrp.reimb.service;

import com.hrp.common.entity.CtrlPayout;

import java.util.List;
import java.util.Map;

/**
 * 报账服务接口
 */
public interface CtrlPayoutService {
    List<CtrlPayout> getMyPayouts(Long empId);
    com.hrp.common.entity.PageResult<CtrlPayout> getMyPayoutsPage(Long empId, Long page, Long size);
    List<CtrlPayout> getPayoutsByStatus(String status);
    com.hrp.common.entity.PageResult<CtrlPayout> getPayoutsByStatusPage(String status, Long page, Long size);
    List<CtrlPayout> getMyApprovalPayouts(String userId);
    com.hrp.common.entity.PageResult<CtrlPayout> getMyApprovalPayoutsPage(String userId, Long page, Long size);
    List<CtrlPayout> getAllPayouts();
    com.hrp.common.entity.PageResult<CtrlPayout> getAllPayoutsPage(Long page, Long size);
    CtrlPayout getById(Long id);
    CtrlPayout getByBillcode(String billcode);
    boolean save(CtrlPayout ctrlPayout);
    boolean save(CtrlPayout ctrlPayout, List<Map<String, Object>> budgetDetails);
    CtrlPayout saveFull(CtrlPayout ctrlPayout, List<Map<String, Object>> budgetDetails, 
                       List<com.hrp.common.entity.CtrlPayoutInvoice> invoices,
                       List<com.hrp.common.entity.CtrlPayoutPayment> payments);
    boolean update(CtrlPayout ctrlPayout);
    boolean update(CtrlPayout ctrlPayout, List<Map<String, Object>> budgetDetails);
    boolean delete(Long id);
    boolean submit(Long payoutId);
    boolean withdraw(Long payoutId);
    boolean approve(Long payoutId, String userId, String opinion, String approverSignature);
    boolean reject(Long payoutId, String userId, String opinion);
    boolean returnPayout(Long payoutId, String returnType, String opinion);
    List<CtrlPayout> getByItemId(Long itemId); // 用于申请冲销：查询APPLY类型的申请单
    List<CtrlPayout> getPayoutByItemId(Long itemId); // 用于报账冲销：查询PAYOUT类型的报账单
    com.hrp.common.entity.PageResult<CtrlPayout> getPage(Long page, Long size, String payoutBillcode, String empName, String payoutTypeId, String status, String startDate, String endDate, String billTypePrefix);
    com.hrp.common.entity.PageResult<CtrlPayout> getPageByApprover(Long page, Long size, String currentUserId, String payoutBillcode, String empName, String payoutTypeId, String status, String startDate, String endDate, String billTypePrefix);
    /**
     * 检查来源申请单号是否已被使用
     * @param sourceApplyNo 来源申请单号
     * @param excludePayoutId 排除的报账单ID（用于更新时排除自己）
     * @return 如果已被使用，返回true
     */
    boolean isSourceApplyNoUsed(String sourceApplyNo, Long excludePayoutId);
    
    /**
     * 检查合同编号是否已被使用
     * @param contractNo 合同编号
     * @param excludePayoutId 排除的报账单ID（用于更新时排除自己）
     * @return 如果已被使用，返回true
     */
    boolean isContractNoUsed(String contractNo, Long excludePayoutId);
}

