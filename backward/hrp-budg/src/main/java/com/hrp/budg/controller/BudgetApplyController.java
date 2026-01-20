package com.hrp.budg.controller;

import com.hrp.common.entity.BudgetApply;
import com.hrp.common.entity.Result;
import com.hrp.common.entity.PageResult;
import com.hrp.budg.service.BudgetApplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 预算申请控制器
 */
@RestController
@RequestMapping("/budg/apply")
@CrossOrigin
public class BudgetApplyController {

    @Autowired
    private BudgetApplyService budgetApplyService;

    @GetMapping("/list")
    public Result<PageResult<BudgetApply>> getList(
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size,
            @RequestParam(value = "applyNo", required = false) String applyNo,
            @RequestParam(value = "itemId", required = false) Long itemId,
            @RequestParam(value = "applicantName", required = false) String applicantName,
            @RequestParam(value = "applicantCode", required = false) String applicantCode,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
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
            PageResult<BudgetApply> pageResult = budgetApplyService.getPageByApprover(page, size, currentUserId, applyNo, itemId, applicantName, status, startDate, endDate);
            return Result.success(pageResult);
        } else {
            PageResult<BudgetApply> pageResult = budgetApplyService.getPage(page, size, applyNo, itemId, applicantName, applicantCode, status, startDate, endDate);
            return Result.success(pageResult);
        }
    }

    @GetMapping("/{id}")
    public Result<BudgetApply> getById(@PathVariable("id") Long id) {
        BudgetApply apply = budgetApplyService.getById(id);
        return Result.success(apply);
    }

    @GetMapping("/no/{applyNo}")
    public Result<BudgetApply> getByNo(@PathVariable("applyNo") String applyNo) {
        BudgetApply apply = budgetApplyService.getByNo(applyNo);
        return Result.success(apply);
    }

    @PostMapping
    public Result<BudgetApply> save(@RequestBody BudgetApply budgetApply, @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 从token获取当前用户账号
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                String account = com.hrp.common.util.JwtUtil.getAccount(token);
                budgetApply.setCreateUser(account);
            }
            BudgetApply saved = budgetApplyService.save(budgetApply);
            if (saved != null) {
                // 返回保存后的对象，包含生成的applyNo等信息
                return Result.success(saved);
            } else {
                return Result.error("新增失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("新增失败: " + e.getMessage());
        }
    }

    @PutMapping
    public Result<BudgetApply> update(@RequestBody BudgetApply budgetApply) {
        BudgetApply updated = budgetApplyService.update(budgetApply);
        return updated != null ? Result.success(updated) : Result.error("更新失败");
    }

    @PostMapping("/submit/{id}")
    public Result<Void> submit(@PathVariable Long id) {
        boolean success = budgetApplyService.submit(id);
        return success ? Result.success() : Result.error("提交失败");
    }

    @PostMapping("/approve/{id}")
    public Result<Void> approve(@PathVariable Long id, @RequestBody(required = false) Map<String, String> request) {
        String opinion = request != null ? request.get("opinion") : null;
        String approverSignature = request != null ? request.get("approverSignature") : null;
        boolean success = budgetApplyService.approve(id, opinion, approverSignature);
        return success ? Result.success() : Result.error("审批失败");
    }

    @PostMapping("/reject/{id}")
    public Result<Void> reject(@PathVariable Long id, @RequestBody(required = false) Map<String, String> request) {
        String opinion = request != null ? request.get("opinion") : null;
        boolean success = budgetApplyService.reject(id, opinion);
        return success ? Result.success() : Result.error("拒绝失败");
    }

    @PostMapping("/return/{id}")
    public Result<Void> returnApply(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String returnType = request != null ? request.get("returnType") : "RETURN_TO_CURRENT";
        String opinion = request != null ? request.get("opinion") : null;
        boolean success = budgetApplyService.returnApply(id, returnType, opinion);
        return success ? Result.success() : Result.error("退回失败");
    }

    @PostMapping("/withdraw/{id}")
    public Result<Void> withdraw(@PathVariable Long id) {
        try {
            boolean success = budgetApplyService.withdraw(id);
            return success ? Result.success() : Result.error("撤回失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("撤回失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        try {
            boolean success = budgetApplyService.delete(id);
            return success ? Result.success() : Result.error("删除失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 根据预算项目ID查询申请单（用于申请冲销）
     */
    @GetMapping("/item/{itemId}")
    public Result<List<BudgetApply>> getByItemId(@PathVariable("itemId") Long itemId) {
        List<BudgetApply> applies = budgetApplyService.getByItemId(itemId);
        return Result.success(applies);
    }
}

