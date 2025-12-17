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
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size) {
        PageResult<BudgetApply> pageResult = budgetApplyService.getPage(page, size);
        return Result.success(pageResult);
    }

    @GetMapping("/{id}")
    public Result<BudgetApply> getById(@PathVariable Long id) {
        BudgetApply apply = budgetApplyService.getById(id);
        return Result.success(apply);
    }

    @PostMapping
    public Result<Void> save(@RequestBody BudgetApply budgetApply) {
        boolean success = budgetApplyService.save(budgetApply);
        return success ? Result.success() : Result.error("新增失败");
    }

    @PutMapping
    public Result<Void> update(@RequestBody BudgetApply budgetApply) {
        boolean success = budgetApplyService.update(budgetApply);
        return success ? Result.success() : Result.error("更新失败");
    }

    @PostMapping("/submit/{id}")
    public Result<Void> submit(@PathVariable Long id) {
        boolean success = budgetApplyService.submit(id);
        return success ? Result.success() : Result.error("提交失败");
    }

    @PostMapping("/approve/{id}")
    public Result<Void> approve(@PathVariable Long id, @RequestBody(required = false) Map<String, String> request) {
        String opinion = request != null ? request.get("opinion") : null;
        boolean success = budgetApplyService.approve(id, opinion);
        return success ? Result.success() : Result.error("审批失败");
    }

    @PostMapping("/reject/{id}")
    public Result<Void> reject(@PathVariable Long id, @RequestBody(required = false) Map<String, String> request) {
        String opinion = request != null ? request.get("opinion") : null;
        boolean success = budgetApplyService.reject(id, opinion);
        return success ? Result.success() : Result.error("拒绝失败");
    }
}

