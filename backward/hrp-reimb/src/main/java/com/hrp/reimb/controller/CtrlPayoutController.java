package com.hrp.reimb.controller;

import com.hrp.common.entity.Result;
import com.hrp.common.entity.CtrlPayout;
import com.hrp.common.entity.CtrlPayoutDTO;
import com.hrp.common.entity.CtrlPayoutInvoice;
import com.hrp.common.entity.CtrlPayoutPayment;
import com.hrp.reimb.service.CtrlPayoutService;
import com.hrp.reimb.service.CtrlPayoutInvoiceService;
import com.hrp.reimb.service.CtrlPayoutPaymentService;
import com.hrp.reimb.feign.AuthServiceClient;
import com.hrp.reimb.feign.ContractServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reimb/payout")
@CrossOrigin
public class CtrlPayoutController {

    @Autowired
    private CtrlPayoutService ctrlPayoutService;

    @Autowired
    private CtrlPayoutInvoiceService ctrlPayoutInvoiceService;

    @Autowired
    private CtrlPayoutPaymentService ctrlPayoutPaymentService;

    @Autowired(required = false)
    private AuthServiceClient authServiceClient;
    
    @Autowired(required = false)
    private ContractServiceClient contractServiceClient;

    /**
     * 我的申请列表
     */
    @GetMapping("/my/{empId}")
    public Result<List<CtrlPayout>> getMyPayouts(@PathVariable("empId") Long empId) {
        List<CtrlPayout> payouts = ctrlPayoutService.getMyPayouts(empId);
        return Result.success(payouts);
    }

    /**
     * 我的申请列表（分页）
     */
    @GetMapping("/my/{empId}/page")
    public Result<com.hrp.common.entity.PageResult<CtrlPayout>> getMyPayoutsPage(
            @PathVariable("empId") Long empId,
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size) {
        com.hrp.common.entity.PageResult<CtrlPayout> pageResult = ctrlPayoutService.getMyPayoutsPage(empId, page, size);
        return Result.success(pageResult);
    }

    /**
     * 根据状态查询申请列表（用于审批）
     */
    @GetMapping("/status/{status}")
    public Result<List<CtrlPayout>> getPayoutsByStatus(@PathVariable("status") String status) {
        List<CtrlPayout> payouts = ctrlPayoutService.getPayoutsByStatus(status);
        return Result.success(payouts);
    }

    /**
     * 根据状态查询申请列表（分页）
     */
    @GetMapping("/status/{status}/page")
    public Result<com.hrp.common.entity.PageResult<CtrlPayout>> getPayoutsByStatusPage(
            @PathVariable("status") String status,
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size) {
        com.hrp.common.entity.PageResult<CtrlPayout> pageResult = ctrlPayoutService.getPayoutsByStatusPage(status, page, size);
        return Result.success(pageResult);
    }

    /**
     * 查询列表（支持审批列表和查询列表）
     */
    @GetMapping("/list")
    public Result<com.hrp.common.entity.PageResult<CtrlPayout>> getList(
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size,
            @RequestParam(value = "payoutBillcode", required = false) String payoutBillcode,
            @RequestParam(value = "empName", required = false) String empName,
            @RequestParam(value = "payoutTypeId", required = false) String payoutTypeId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "billTypePrefix", required = false) String billTypePrefix, // SQD或BZD前缀
            @RequestParam(value = "isApprovalList", required = false, defaultValue = "false") Boolean isApprovalList,
            @RequestHeader(value = "Authorization", required = false) String token) {
        // 如果是审批列表，使用审批人过滤（根据userId查询待审批任务）
        if (isApprovalList != null && isApprovalList) {
            String currentUserId = null;
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                currentUserId = com.hrp.common.util.JwtUtil.getUserId(token);
            }
            if (currentUserId == null || currentUserId.trim().isEmpty()) {
                return Result.error("未获取到当前用户信息");
            }
            com.hrp.common.entity.PageResult<CtrlPayout> pageResult = ctrlPayoutService.getPageByApprover(
                    page, size, currentUserId, payoutBillcode, empName, payoutTypeId, status, startDate, endDate, billTypePrefix);
            return Result.success(pageResult);
        } else {
            // 查询列表：显示所有数据，根据单号前缀过滤
            com.hrp.common.entity.PageResult<CtrlPayout> pageResult = ctrlPayoutService.getPage(
                    page, size, payoutBillcode, empName, payoutTypeId, status, startDate, endDate, billTypePrefix);
            return Result.success(pageResult);
        }
    }

    /**
     * 根据ID获取申请详情（仅主表）
     */
    @GetMapping("/{id}")
    public Result<CtrlPayout> getById(@PathVariable("id") Long id) {
        CtrlPayout payout = ctrlPayoutService.getById(id);
        return Result.success(payout);
    }

    /**
     * 根据申请单号获取申请详情（仅主表）
     */
    @GetMapping("/billcode/{payoutBillcode}")
    public Result<CtrlPayout> getByBillcode(@PathVariable("payoutBillcode") String payoutBillcode) {
        CtrlPayout payout = ctrlPayoutService.getByBillcode(payoutBillcode);
        if (payout == null) {
            return Result.error("单据不存在");
        }
        return Result.success(payout);
    }

    /**
     * 根据ID获取完整信息（包括明细、发票、支付清单、审批记录）
     */
    @GetMapping("/{id}/detail")
    public Result<CtrlPayoutDTO> getDetailById(@PathVariable("id") Long id) {
        CtrlPayout payout = ctrlPayoutService.getById(id);
        if (payout == null) {
            return Result.error("单据不存在");
        }
        
        CtrlPayoutDTO dto = new CtrlPayoutDTO();
        dto.setPayout(payout);
        
        // 如果是报账单，获取发票、支付清单（明细表已删除）
        if ("PAYOUT".equals(payout.getBillType())) {
            dto.setInvoices(ctrlPayoutInvoiceService.getByPayoutId(id));
            dto.setPayments(ctrlPayoutPaymentService.getByPayoutId(id));
        }
        
        // 审批记录从流程任务中获取，不再使用ctrl_payout_approvel表
        
        return Result.success(dto);
    }

    /**
     * 根据申请单号获取完整信息（包括明细、发票、支付清单、审批记录）
     */
    @GetMapping("/billcode/{payoutBillcode}/detail")
    public Result<CtrlPayoutDTO> getDetailByBillcode(@PathVariable("payoutBillcode") String payoutBillcode) {
        CtrlPayout payout = ctrlPayoutService.getByBillcode(payoutBillcode);
        if (payout == null) {
            return Result.error("单据不存在");
        }
        
        CtrlPayoutDTO dto = new CtrlPayoutDTO();
        dto.setPayout(payout);
        
        // 如果是报账单，获取发票、支付清单（明细表已删除）
        if ("PAYOUT".equals(payout.getBillType())) {
            dto.setInvoices(ctrlPayoutInvoiceService.getByPayoutId(payout.getPayoutId()));
            dto.setPayments(ctrlPayoutPaymentService.getByPayoutId(payout.getPayoutId()));
        }
        
        // 审批记录从流程任务中获取，不再使用ctrl_payout_approvel表
        
        return Result.success(dto);
    }

    /**
     * 新增申请
     */
    @PostMapping
    public Result<CtrlPayout> save(@RequestBody Map<String, Object> request, @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            CtrlPayout ctrlPayout = convertToCtrlPayout(request);
            // 从token获取当前用户账号并设置createUser
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                String account = com.hrp.common.util.JwtUtil.getAccount(token);
                if (account != null) {
                    ctrlPayout.setCreateUser(account);
                }
            }
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> budgetDetails = (List<Map<String, Object>>) request.get("budgetDetails");
            boolean success = ctrlPayoutService.save(ctrlPayout, budgetDetails);
            if (success) {
                return Result.success(ctrlPayout);
            }
            return Result.error("新增失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("新增失败: " + e.getMessage());
        }
    }

    /**
     * 更新申请
     */
    @PutMapping
    public Result<CtrlPayout> update(@RequestBody Map<String, Object> request) {
        try {
            CtrlPayout ctrlPayout = convertToCtrlPayout(request);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> budgetDetails = (List<Map<String, Object>>) request.get("budgetDetails");
            boolean success = ctrlPayoutService.update(ctrlPayout, budgetDetails);
            if (success) {
                return Result.success(ctrlPayout);
            }
            return Result.error("更新失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新失败: " + e.getMessage());
        }
    }
    
    /**
     * 将Map转换为CtrlPayout对象
     */
    private CtrlPayout convertToCtrlPayout(Map<String, Object> map) {
        CtrlPayout ctrlPayout = new CtrlPayout();
        if (map.get("payoutId") != null) ctrlPayout.setPayoutId(Long.valueOf(map.get("payoutId").toString()));
        if (map.get("payoutBillcode") != null) ctrlPayout.setPayoutBillcode(map.get("payoutBillcode").toString());
        if (map.get("billType") != null) ctrlPayout.setBillType(map.get("billType").toString());
        if (map.get("empId") != null) ctrlPayout.setEmpId(Long.valueOf(map.get("empId").toString()));
        if (map.get("empCode") != null) ctrlPayout.setEmpCode(map.get("empCode").toString());
        if (map.get("empName") != null) ctrlPayout.setEmpName(map.get("empName").toString());
        if (map.get("deptId") != null) ctrlPayout.setDeptId(Long.valueOf(map.get("deptId").toString()));
        if (map.get("isNurse") != null) ctrlPayout.setIsNurse(Long.valueOf(map.get("isNurse").toString()));
        if (map.get("payoutTypeId") != null) ctrlPayout.setPayoutTypeId(map.get("payoutTypeId").toString());
        if (map.get("applyAmount") != null) ctrlPayout.setApplyAmount(new java.math.BigDecimal(map.get("applyAmount").toString()));
        if (map.get("applyReason") != null) ctrlPayout.setApplyReason(map.get("applyReason").toString());
        if (map.get("status") != null) ctrlPayout.setStatus(map.get("status").toString());
        if (map.get("processDefinitionId") != null) ctrlPayout.setProcessDefinitionId(Long.valueOf(map.get("processDefinitionId").toString()));
        if (map.get("processInstanceId") != null) ctrlPayout.setProcessInstanceId(Long.valueOf(map.get("processInstanceId").toString()));
        if (map.get("budgetId") != null) ctrlPayout.setBudgetId(Long.valueOf(map.get("budgetId").toString()));
        if (map.get("budgetItemId") != null) ctrlPayout.setBudgetItemId(Long.valueOf(map.get("budgetItemId").toString()));
        if (map.get("remark") != null) ctrlPayout.setRemark(map.get("remark").toString());
        if (map.get("createUser") != null) ctrlPayout.setCreateUser(map.get("createUser").toString());
        if (map.get("templateConfigId") != null) ctrlPayout.setTemplateConfigId(Long.valueOf(map.get("templateConfigId").toString()));
        if (map.get("mainAttachId") != null) ctrlPayout.setMainAttachId(map.get("mainAttachId").toString());
        if (map.get("sourceApplyNo") != null) ctrlPayout.setSourceApplyNo(map.get("sourceApplyNo").toString());
        return ctrlPayout;
    }

    /**
     * 删除申请
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = ctrlPayoutService.delete(id);
        return success ? Result.success() : Result.error("删除失败");
    }

    /**
     * 提交申请
     */
    @PostMapping("/{id}/submit")
    public Result<String> submit(@PathVariable Long id) {
        boolean success = ctrlPayoutService.submit(id);
        if (success) {
            return Result.success("提交成功");
        }
        return Result.error("提交失败");
    }

    /**
     * 撤回申请
     */
    @PostMapping("/{id}/withdraw")
    public Result<Void> withdraw(@PathVariable Long id) {
        boolean success = ctrlPayoutService.withdraw(id);
        return success ? Result.success() : Result.error("撤回失败");
    }

    /**
     * 获取我的审批列表
     */
    @GetMapping("/my-approval/{userId}")
    public Result<List<CtrlPayout>> getMyApprovalPayouts(@PathVariable("userId") String userId) {
        List<CtrlPayout> payouts = ctrlPayoutService.getMyApprovalPayouts(userId);
        return Result.success(payouts);
    }

    /**
     * 获取我的审批列表（分页）
     */
    @GetMapping("/my-approval/{userId}/page")
    public Result<com.hrp.common.entity.PageResult<CtrlPayout>> getMyApprovalPayoutsPage(
            @PathVariable("userId") String userId,
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size) {
        com.hrp.common.entity.PageResult<CtrlPayout> pageResult = ctrlPayoutService.getMyApprovalPayoutsPage(userId, page, size);
        return Result.success(pageResult);
    }

    /**
     * 获取所有申请（管理员查询）
     */
    @GetMapping("/all")
    public Result<List<CtrlPayout>> getAllPayouts() {
        List<CtrlPayout> payouts = ctrlPayoutService.getAllPayouts();
        return Result.success(payouts);
    }

    /**
     * 获取所有申请（分页）
     */
    @GetMapping("/all/page")
    public Result<com.hrp.common.entity.PageResult<CtrlPayout>> getAllPayoutsPage(
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size) {
        com.hrp.common.entity.PageResult<CtrlPayout> pageResult = ctrlPayoutService.getAllPayoutsPage(page, size);
        return Result.success(pageResult);
    }

    /**
     * 审批通过
     */
    @PostMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable("id") Long id, @RequestBody Map<String, String> request) {
        String userId = request != null ? request.get("userId") : null;
        String opinion = request != null ? request.get("opinion") : null;
        String approverSignature = request != null ? request.get("approverSignature") : null;
        boolean success = ctrlPayoutService.approve(id, userId, opinion, approverSignature);
        return success ? Result.success() : Result.error("审批失败");
    }

    /**
     * 审批驳回
     */
    @PostMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable("id") Long id, @RequestParam(value = "userId") String userId, @RequestParam(value = "opinion", required = false) String opinion) {
        boolean success = ctrlPayoutService.reject(id, userId, opinion);
        return success ? Result.success() : Result.error("驳回失败");
    }

    /**
     * 退回申请
     */
    @PostMapping("/{id}/return")
    public Result<Void> returnPayout(@PathVariable("id") Long id, @RequestBody Map<String, String> request) {
        String returnType = request != null ? request.get("returnType") : "RETURN_TO_CURRENT";
        String opinion = request != null ? request.get("opinion") : null;
        boolean success = ctrlPayoutService.returnPayout(id, returnType, opinion);
        return success ? Result.success() : Result.error("退回失败");
    }

    /**
     * 获取审批记录（从流程任务中获取，不再使用ctrl_payout_approvel表）
     */
    @GetMapping("/{id}/approvals")
    public Result<List<Object>> getApprovals(@PathVariable("id") Long id) {
        // 审批记录应该从流程任务中获取，这里返回空列表
        // 前端应该调用流程任务接口获取审批记录
        return Result.success(new java.util.ArrayList<>());
    }

    /**
     * 根据预算项目ID查询申请单（用于申请冲销，只返回bill_type='APPLY'且status='APPROVED'的记录）
     */
    @GetMapping("/item/{itemId}")
    public Result<List<CtrlPayout>> getByItemId(@PathVariable("itemId") Long itemId) {
        List<CtrlPayout> payouts = ctrlPayoutService.getByItemId(itemId);
        return Result.success(payouts);
    }

    /**
     * 根据预算项目ID查询报账单（用于报账冲销，只返回bill_type='PAYOUT'且status='APPROVED'的记录）
     */
    @GetMapping("/item/{itemId}/payout")
    public Result<List<CtrlPayout>> getPayoutByItemId(@PathVariable("itemId") Long itemId) {
        List<CtrlPayout> payouts = ctrlPayoutService.getPayoutByItemId(itemId);
        return Result.success(payouts);
    }
    
    /**
     * 根据业务单号获取预算明细记录
     */
    @GetMapping("/budget-details/{businessNo}")
    public Result<List<com.hrp.common.entity.BudgetDetailRecord>> getBudgetDetails(@PathVariable("businessNo") String businessNo) {
        // 通过Feign客户端调用budg服务
        try {
            if (ctrlPayoutService instanceof com.hrp.reimb.service.impl.CtrlPayoutServiceImpl) {
                com.hrp.reimb.service.impl.CtrlPayoutServiceImpl serviceImpl = 
                    (com.hrp.reimb.service.impl.CtrlPayoutServiceImpl) ctrlPayoutService;
                com.hrp.common.entity.Result<List<com.hrp.common.entity.BudgetDetailRecord>> result = 
                    serviceImpl.getBudgetDetailsByBusinessNo(businessNo);
                if (result != null && result.getCode() == 200) {
                    return Result.success(result.getData());
                }
            }
        } catch (Exception e) {
            System.err.println("获取预算明细失败: " + e.getMessage());
        }
        return Result.success(new java.util.ArrayList<>());
    }

    /**
     * 检查来源申请单号是否已被使用
     */
    @GetMapping("/check-source-apply-no/{sourceApplyNo}")
    public Result<Boolean> checkSourceApplyNo(@PathVariable("sourceApplyNo") String sourceApplyNo, 
                                               @RequestParam(value = "excludePayoutId", required = false) Long excludePayoutId) {
        boolean isUsed = ctrlPayoutService.isSourceApplyNoUsed(sourceApplyNo, excludePayoutId);
        return Result.success(isUsed);
    }

    /**
     * 检查合同编号是否已被使用
     */
    @GetMapping("/check-contract-no/{contractNo}")
    public Result<Boolean> checkContractNo(@PathVariable("contractNo") String contractNo, 
                                           @RequestParam(value = "excludePayoutId", required = false) Long excludePayoutId) {
        boolean isUsed = ctrlPayoutService.isContractNoUsed(contractNo, excludePayoutId);
        return Result.success(isUsed);
    }

    /**
     * 保存报账单完整信息（包括明细、发票、支付清单）
     */
    @PostMapping("/payout/save")
    public Result<CtrlPayout> savePayout(@RequestBody CtrlPayoutDTO dto, @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            CtrlPayout payout = dto.getPayout();
            if (payout == null) {
                return Result.error("主表信息不能为空");
            }
        
            // 从token获取当前用户账号并设置createUser和员工信息
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                String account = com.hrp.common.util.JwtUtil.getAccount(token);
                if (account != null) {
                    if (payout.getCreateUser() == null) {
                        payout.setCreateUser(account);
                    }
                    
                    // 如果empCode、empName、deptId为空，尝试从用户信息中获取
                    if ((payout.getEmpCode() == null || payout.getEmpCode().isEmpty()) || 
                        (payout.getEmpName() == null || payout.getEmpName().isEmpty()) ||
                        payout.getDeptId() == null) {
                        try {
                            // 通过Feign调用用户服务获取用户信息
                            if (authServiceClient != null) {
                                com.hrp.common.entity.Result<com.hrp.common.entity.User> userResult = 
                                    authServiceClient.getUserByAccount(account);
                                if (userResult != null && userResult.getCode() == 200 && userResult.getData() != null) {
                                    com.hrp.common.entity.User user = userResult.getData();
                                    // 设置员工信息
                                    if (payout.getEmpCode() == null || payout.getEmpCode().isEmpty()) {
                                        payout.setEmpCode(user.getAccount() != null ? user.getAccount() : account);
                                    }
                                    if (payout.getEmpName() == null || payout.getEmpName().isEmpty()) {
                                        payout.setEmpName(user.getName());
                                    }
                                    if (payout.getEmpId() == null) {
                                        // 如果user有empId字段，使用它
                                        if (user.getEmpId() != null) {
                                            payout.setEmpId(Long.parseLong(user.getEmpId().toString()));
                                        }
                                    }
                                    // 获取部门信息
                                    if (payout.getDeptId() == null) {
                                        // 如果User对象有deptId字段，直接使用
                                        if (user.getDeptId() != null) {
                                            payout.setDeptId(user.getDeptId());
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("通过账号获取用户信息失败: " + e.getMessage());
                            // 如果获取失败，至少设置账号
                            if (payout.getEmpCode() == null || payout.getEmpCode().isEmpty()) {
                                payout.setEmpCode(account);
                            }
                        }
                    }
                }
            }
            
            // 设置单据类型为报账单
            payout.setBillType("PAYOUT");
            
            // 检查来源申请单号是否已被使用
            if (payout.getSourceApplyNo() != null && !payout.getSourceApplyNo().trim().isEmpty()) {
                boolean isUsed = ctrlPayoutService.isSourceApplyNoUsed(payout.getSourceApplyNo(), null);
                if (isUsed) {
                    return Result.error("该申请单号已被其他报账单关联，不能重复关联");
                }
            }
            
            // 检查合同编号是否已被使用，并校验合同金额
            if (payout.getContractNo() != null && !payout.getContractNo().trim().isEmpty()) {
                // 检查合同编号是否已被使用
                boolean isContractUsed = ctrlPayoutService.isContractNoUsed(payout.getContractNo(), null);
                if (isContractUsed) {
                    return Result.error("该合同编号已被其他报账单关联，不能重复关联");
                }
                
                // 校验报账金额与合同金额一致
                if (contractServiceClient != null) {
                    try {
                        com.hrp.common.entity.Result<com.hrp.common.entity.PactMain> contractResult = contractServiceClient.getContractByContractNo(payout.getContractNo());
                        if (contractResult != null && contractResult.getCode() == 200 && contractResult.getData() != null) {
                            com.hrp.common.entity.PactMain contract = contractResult.getData();
                            if (contract.getContractAmount() != null && payout.getApplyAmount() != null) {
                                java.math.BigDecimal contractAmount = contract.getContractAmount();
                                java.math.BigDecimal payoutAmount = payout.getApplyAmount();
                                if (contractAmount.compareTo(payoutAmount) != 0) {
                                    return Result.error("报账金额（¥" + payoutAmount + "）与合同金额（¥" + contractAmount + "）不一致，请核对后重试");
                                }
                            }
                        } else {
                            return Result.error("合同编号不存在或无法获取合同信息");
                        }
                    } catch (Exception e) {
                        System.err.println("获取合同信息失败: " + e.getMessage());
                        return Result.error("获取合同信息失败，请检查合同编号是否正确");
                    }
                }
            }
            
            // 使用完整的保存方法，所有操作在同一事务中
            @SuppressWarnings("unchecked")
            List<java.util.Map<String, Object>> budgetDetails = dto.getBudgetDetails();
            CtrlPayout savedPayout = ctrlPayoutService.saveFull(payout, budgetDetails, dto.getInvoices(), dto.getPayments());
            
            return Result.success(savedPayout); // 返回保存后的payout对象，包含payoutId
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("保存失败: " + e.getMessage());
        }
    }

    /**
     * 更新报账单完整信息
     */
    @PutMapping("/payout/update")
    public Result<Void> updatePayout(@RequestBody CtrlPayoutDTO dto) {
        CtrlPayout payout = dto.getPayout();
        if (payout == null || payout.getPayoutId() == null) {
            return Result.error("主表信息不能为空");
        }
        
        // 检查来源申请单号是否已被使用（排除当前报账单）
        if (payout.getSourceApplyNo() != null && !payout.getSourceApplyNo().trim().isEmpty()) {
            boolean isUsed = ctrlPayoutService.isSourceApplyNoUsed(payout.getSourceApplyNo(), payout.getPayoutId());
            if (isUsed) {
                return Result.error("该申请单号已被其他报账单关联，不能重复关联");
            }
        }
        
        // 检查合同编号是否已被使用（排除当前报账单），并校验合同金额
        if (payout.getContractNo() != null && !payout.getContractNo().trim().isEmpty()) {
            // 检查合同编号是否已被使用
            boolean isContractUsed = ctrlPayoutService.isContractNoUsed(payout.getContractNo(), payout.getPayoutId());
            if (isContractUsed) {
                return Result.error("该合同编号已被其他报账单关联，不能重复关联");
            }
            
            // 校验报账金额与合同金额一致
            if (contractServiceClient != null) {
                try {
                    com.hrp.common.entity.Result<com.hrp.common.entity.PactMain> contractResult = contractServiceClient.getContractByContractNo(payout.getContractNo());
                    if (contractResult != null && contractResult.getCode() == 200 && contractResult.getData() != null) {
                        com.hrp.common.entity.PactMain contract = contractResult.getData();
                        if (contract.getContractAmount() != null && payout.getApplyAmount() != null) {
                            java.math.BigDecimal contractAmount = contract.getContractAmount();
                            java.math.BigDecimal payoutAmount = payout.getApplyAmount();
                            if (contractAmount.compareTo(payoutAmount) != 0) {
                                return Result.error("报账金额（¥" + payoutAmount + "）与合同金额（¥" + contractAmount + "）不一致，请核对后重试");
                            }
                        }
                    } else {
                        return Result.error("合同编号不存在或无法获取合同信息");
                    }
                } catch (Exception e) {
                    System.err.println("获取合同信息失败: " + e.getMessage());
                    return Result.error("获取合同信息失败，请检查合同编号是否正确");
                }
            }
        }
        
        // 更新主表和预算明细（使用带预算明细的update方法）
        @SuppressWarnings("unchecked")
        List<java.util.Map<String, Object>> budgetDetails = dto.getBudgetDetails();
        boolean success = ctrlPayoutService.update(payout, budgetDetails);
        if (!success) {
            return Result.error("更新主表失败");
        }
        
        // 删除旧的发票、支付清单（明细表已删除）
        ctrlPayoutInvoiceService.deleteByPayoutId(payout.getPayoutId());
        ctrlPayoutPaymentService.deleteByPayoutId(payout.getPayoutId());
        
        // 保存新的发票、支付清单
        if (dto.getInvoices() != null && !dto.getInvoices().isEmpty()) {
            for (CtrlPayoutInvoice invoice : dto.getInvoices()) {
                invoice.setPayoutId(payout.getPayoutId());
            }
            ctrlPayoutInvoiceService.saveBatch(dto.getInvoices());
        }
        
        if (dto.getPayments() != null && !dto.getPayments().isEmpty()) {
            for (CtrlPayoutPayment payment : dto.getPayments()) {
                payment.setPayoutId(payout.getPayoutId());
            }
            ctrlPayoutPaymentService.saveBatch(dto.getPayments());
        }
        
        return Result.success();
    }

    /**
     * 更新报账单的发票和支付清单
     */
    @PutMapping("/{payoutId}/invoices-payments")
    public Result<Void> updateInvoicesAndPayments(@PathVariable("payoutId") Long payoutId, @RequestBody CtrlPayoutDTO dto) {
        // 删除旧的发票、支付清单
        ctrlPayoutInvoiceService.deleteByPayoutId(payoutId);
        ctrlPayoutPaymentService.deleteByPayoutId(payoutId);
        
        // 保存新的发票、支付清单
        if (dto.getInvoices() != null && !dto.getInvoices().isEmpty()) {
            for (CtrlPayoutInvoice invoice : dto.getInvoices()) {
                invoice.setPayoutId(payoutId);
            }
            ctrlPayoutInvoiceService.saveBatch(dto.getInvoices());
        }
        
        if (dto.getPayments() != null && !dto.getPayments().isEmpty()) {
            for (CtrlPayoutPayment payment : dto.getPayments()) {
                payment.setPayoutId(payoutId);
            }
            ctrlPayoutPaymentService.saveBatch(dto.getPayments());
        }
        
        return Result.success();
    }
}

