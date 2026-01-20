package com.hrp.contract.controller;

import com.hrp.common.entity.PactMain;
import com.hrp.common.entity.Result;
import com.hrp.contract.service.PactMainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/contract")
@CrossOrigin
public class PactMainController {

    @Autowired
    private PactMainService pactMainService;
    
    @Autowired(required = false)
    private com.hrp.contract.feign.AuthServiceClient authServiceClient;

    @GetMapping("/list")
    public Result<List<PactMain>> getAll() {
        List<PactMain> contracts = pactMainService.getAll();
        return Result.success(contracts);
    }

    @GetMapping("/status/{status}")
    public Result<List<PactMain>> getByStatus(@PathVariable("status") String status) {
        List<PactMain> contracts = pactMainService.getByStatus(status);
        return Result.success(contracts);
    }

    @GetMapping("/status/{status}/page")
    public Result<com.hrp.common.entity.PageResult<PactMain>> getByStatusPage(
            @PathVariable("status") String status,
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size) {
        com.hrp.common.entity.PageResult<PactMain> pageResult = pactMainService.getByStatusPage(status, page, size);
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    public Result<PactMain> getById(@PathVariable("id") Long id) {
        PactMain contract = pactMainService.getById(id);
        return Result.success(contract);
    }

    @GetMapping("/contract-no/{contractNo}")
    public Result<PactMain> getByContractNo(@PathVariable("contractNo") String contractNo) {
        PactMain contract = pactMainService.getByContractNo(contractNo);
        return Result.success(contract);
    }

    @PostMapping
    public Result<PactMain> save(@RequestBody PactMain pactMain) {
        PactMain saved = pactMainService.save(pactMain);
        return saved != null ? Result.success(saved) : Result.error("保存失败");
    }

    @PutMapping
    public Result<Void> update(@RequestBody PactMain pactMain) {
        boolean success = pactMainService.update(pactMain);
        return success ? Result.success() : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        boolean success = pactMainService.delete(id);
        return success ? Result.success() : Result.error("删除失败，只有草稿、已撤回或已拒绝状态的合同才能删除");
    }

    /**
     * 提交合同（通过合同ID）
     */
    @PostMapping("/{id}/submit")
    public Result<String> submit(@PathVariable("id") Long id) {
        boolean success = pactMainService.submit(id);
        if (success) {
            return Result.success("提交成功");
        }
        return Result.error("提交失败");
    }

    /**
     * 提交合同（通过合同编号）
     */
    @PostMapping("/submit-by-no/{contractNo}")
    public Result<String> submitByContractNo(@PathVariable("contractNo") String contractNo) {
        boolean success = pactMainService.submitByContractNo(contractNo);
        if (success) {
            return Result.success("提交成功");
        }
        return Result.error("提交失败");
    }

    /**
     * 撤回合同
     */
    @PostMapping("/{id}/withdraw")
    public Result<Void> withdraw(@PathVariable("id") Long id) {
        boolean success = pactMainService.withdraw(id);
        return success ? Result.success() : Result.error("撤回失败");
    }

    /**
     * 获取我的审批列表
     */
    @GetMapping("/my-approval/{userId}")
    public Result<List<PactMain>> getMyApprovalContracts(@PathVariable("userId") String userId) {
        List<PactMain> contracts = pactMainService.getMyApprovalContracts(userId);
        return Result.success(contracts);
    }

    /**
     * 分页查询合同列表（支持多条件查询）
     */
    @GetMapping("/page")
    public Result<com.hrp.common.entity.PageResult<PactMain>> getPage(
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size,
            @RequestParam(value = "contractNo", required = false) String contractNo,
            @RequestParam(value = "contractName", required = false) String contractName,
            @RequestParam(value = "contractType", required = false) String contractType,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "isApprovalList", required = false) Boolean isApprovalList,
            @RequestParam(value = "skipEmpIdFilter", required = false) Boolean skipEmpIdFilter,
            @RequestHeader(value = "Authorization", required = false) String token) {
        // 如果是审批列表，使用getPageByApprover
        if (isApprovalList != null && isApprovalList) {
            String currentUserId = null;
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                currentUserId = com.hrp.common.util.JwtUtil.getUserId(token);
            }
            if (currentUserId == null || currentUserId.trim().isEmpty()) {
                return Result.error("未获取到当前用户信息");
            }
            com.hrp.common.entity.PageResult<PactMain> pageResult = pactMainService.getPageByApprover(page, size, currentUserId, contractNo, contractName, contractType, status, startDate, endDate);
            return Result.success(pageResult);
        } else {
            // 如果skipEmpIdFilter为true（工作台），不进行empId过滤
            Long currentEmpId = null;
            if (skipEmpIdFilter == null || !skipEmpIdFilter) {
                // 从token中获取当前用户的empId（用于合同起草页面过滤）
                if (token != null && token.startsWith("Bearer ")) {
                    try {
                        String tokenStr = token.substring(7);
                        String account = com.hrp.common.util.JwtUtil.getAccount(tokenStr);
                        if (account != null && !account.trim().isEmpty() && authServiceClient != null) {
                            // 通过账号获取用户信息，然后获取empId
                            com.hrp.common.entity.Result<com.hrp.common.entity.User> userResult = authServiceClient.getUserByAccount(account);
                            if (userResult != null && userResult.getCode() == 200 && userResult.getData() != null) {
                                com.hrp.common.entity.User user = userResult.getData();
                                currentEmpId = user.getEmpId();
                            }
                        }
                    } catch (Exception e) {
                        // 如果获取empId失败，不影响查询，只是不进行过滤
                        System.err.println("获取当前用户empId失败: " + e.getMessage());
                    }
                }
            }
            com.hrp.common.entity.PageResult<PactMain> pageResult = pactMainService.getPage(page, size, contractNo, contractName, contractType, status, startDate, endDate, currentEmpId);
            return Result.success(pageResult);
        }
    }

    /**
     * 审批通过
     */
    @PostMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable("id") Long id, @RequestBody java.util.Map<String, String> request) {
        String userId = request != null ? request.get("userId") : null;
        String opinion = request != null ? request.get("opinion") : null;
        String signature = request != null ? request.get("signature") : null;
        boolean success = pactMainService.approve(id, userId, opinion, signature);
        return success ? Result.success() : Result.error("审批失败");
    }

    /**
     * 审批驳回
     */
    @PostMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable("id") Long id, @RequestParam(value = "userId") String userId, @RequestParam(value = "opinion", required = false) String opinion) {
        boolean success = pactMainService.reject(id, userId, opinion);
        return success ? Result.success() : Result.error("驳回失败");
    }

    /**
     * 退回合同
     */
    @PostMapping("/{id}/return")
    public Result<Void> returnContract(@PathVariable("id") Long id, @RequestBody Map<String, String> request) {
        String returnType = request != null ? request.get("returnType") : "RETURN_TO_CURRENT";
        String opinion = request != null ? request.get("opinion") : null;
        boolean success = pactMainService.returnContract(id, returnType, opinion);
        return success ? Result.success() : Result.error("退回失败");
    }

    /**
     * 归档合同
     */
    @PostMapping("/{id}/archive")
    public Result<Void> archive(@PathVariable("id") Long id) {
        boolean success = pactMainService.archive(id);
        return success ? Result.success() : Result.error("归档失败");
    }

    /**
     * 获取下一个审批人
     */
    @GetMapping("/{id}/next-approver")
    public Result<String> getNextApprover(@PathVariable("id") Long id) {
        String nextApprover = pactMainService.getNextApprover(id);
        return Result.success(nextApprover);
    }

    /**
     * 查询已审批的合同（用于合同执行页面）
     */
    @GetMapping("/approved/page")
    public Result<com.hrp.common.entity.PageResult<PactMain>> getApprovedContractsPage(
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size,
            @RequestParam(value = "contractNo", required = false) String contractNo,
            @RequestParam(value = "contractName", required = false) String contractName,
            @RequestParam(value = "contractType", required = false) String contractType,
            @RequestParam(value = "executionStatus", required = false) String executionStatus) {
        com.hrp.common.entity.PageResult<PactMain> pageResult = 
            pactMainService.getApprovedContractsPage(page, size, contractNo, contractName, contractType, executionStatus);
        return Result.success(pageResult);
    }

    /**
     * 查询采购合同列表（只查询合同类型为PURCHASE的合同，状态为已审批）
     */
    @GetMapping("/purchase/list")
    public Result<List<PactMain>> getPurchaseContracts(@RequestParam(value = "contractNo", required = false) String contractNo) {
        // 查询采购合同，不进行员工过滤（empId传null），状态为已审批
        com.hrp.common.entity.PageResult<PactMain> pageResult = pactMainService.getPage(
            1L, 1000L, contractNo, null, "PURCHASE", "APPROVED", null, null, null);
        return Result.success(pageResult.getRecords());
    }

    /**
     * 失效合同（手动修改）
     */
    @PostMapping("/{id}/invalidate")
    public Result<Void> invalidate(@PathVariable("id") Long id) {
        boolean success = pactMainService.invalidate(id);
        return success ? Result.success() : Result.error("失效失败");
    }

    /**
     * 手动归档合同
     */
    @PostMapping("/{id}/archive-manual")
    public Result<Void> archiveManual(@PathVariable("id") Long id) {
        try {
            boolean success = pactMainService.archiveManual(id);
            return success ? Result.success() : Result.error("归档失败");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("归档失败：" + e.getMessage());
        }
    }

    /**
     * 手动触发更新合同执行状态
     */
    @PostMapping("/update-execution-status")
    public Result<Void> updateExecutionStatus() {
        try {
            pactMainService.updateExecutionStatus();
            return Result.success();
        } catch (Exception e) {
            return Result.error("更新执行状态失败：" + e.getMessage());
        }
    }
}
