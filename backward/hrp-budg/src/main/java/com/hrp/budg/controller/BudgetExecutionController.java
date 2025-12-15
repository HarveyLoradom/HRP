package com.hrp.budg.controller;

import com.hrp.common.entity.BudgetExecution;
import com.hrp.common.entity.Result;
import com.hrp.budg.service.BudgetExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 预算执行单管理控制器
 */
@RestController
@RequestMapping("/budg/execution")
@CrossOrigin
public class BudgetExecutionController {

    @Autowired
    private BudgetExecutionService budgetExecutionService;

    @GetMapping("/{id}")
    public Result<BudgetExecution> getById(@PathVariable Long id) {
        BudgetExecution execution = budgetExecutionService.getById(id);
        return Result.success(execution);
    }

    @GetMapping("/no/{no}")
    public Result<BudgetExecution> getByNo(@PathVariable String no) {
        BudgetExecution execution = budgetExecutionService.getByNo(no);
        return Result.success(execution);
    }

    @GetMapping("/list")
    public Result<List<BudgetExecution>> getAll() {
        List<BudgetExecution> list = budgetExecutionService.getAll();
        return Result.success(list);
    }

    @GetMapping("/budget/{budgetId}")
    public Result<List<BudgetExecution>> getByBudgetId(@PathVariable Long budgetId) {
        List<BudgetExecution> list = budgetExecutionService.getByBudgetId(budgetId);
        return Result.success(list);
    }

    @GetMapping("/business")
    public Result<List<BudgetExecution>> getByBusinessId(@RequestParam String businessType, @RequestParam Long businessId) {
        List<BudgetExecution> list = budgetExecutionService.getByBusinessId(businessType, businessId);
        return Result.success(list);
    }

    @PostMapping
    public Result<Void> save(@RequestBody BudgetExecution budgetExecution) {
        boolean success = budgetExecutionService.save(budgetExecution);
        return success ? Result.success() : Result.error("新增失败");
    }

    @PutMapping
    public Result<Void> update(@RequestBody BudgetExecution budgetExecution) {
        boolean success = budgetExecutionService.update(budgetExecution);
        return success ? Result.success() : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = budgetExecutionService.delete(id);
        return success ? Result.success() : Result.error("删除失败");
    }
}








