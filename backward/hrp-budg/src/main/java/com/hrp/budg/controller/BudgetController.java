package com.hrp.budg.controller;

import com.hrp.common.entity.Budget;
import com.hrp.common.entity.Result;
import com.hrp.budg.service.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 预算管理控制器
 */
@RestController
@RequestMapping("/budg/budget")
@CrossOrigin
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    @GetMapping("/{id}")
    public Result<Budget> getById(@PathVariable Long id) {
        Budget budget = budgetService.getById(id);
        return Result.success(budget);
    }

    @GetMapping("/no/{no}")
    public Result<Budget> getByNo(@PathVariable String no) {
        Budget budget = budgetService.getByNo(no);
        return Result.success(budget);
    }

    @GetMapping("/list")
    public Result<List<Budget>> getAll() {
        List<Budget> list = budgetService.getAll();
        return Result.success(list);
    }

    @GetMapping("/year/{year}")
    public Result<List<Budget>> getByYear(@PathVariable String year) {
        List<Budget> list = budgetService.getByYear(year);
        return Result.success(list);
    }

    @GetMapping("/item/{itemId}")
    public Result<List<Budget>> getByItemId(@PathVariable Long itemId) {
        List<Budget> list = budgetService.getByItemId(itemId);
        return Result.success(list);
    }

    @GetMapping("/subject/{subjectId}")
    public Result<List<Budget>> getBySubjectAndItem(@PathVariable Long subjectId, @RequestParam(required = false) Long itemId) {
        if (itemId != null) {
            List<Budget> list = budgetService.getBySubjectAndItem(subjectId, itemId);
            return Result.success(list);
        } else {
            List<Budget> list = budgetService.getBySubjectId(subjectId);
            return Result.success(list);
        }
    }

    @GetMapping("/check")
    public Result<Boolean> checkBudgetAmount(@RequestParam Long budgetId, @RequestParam BigDecimal amount) {
        boolean available = budgetService.checkBudgetAmount(budgetId, amount);
        return Result.success(available);
    }

    @PostMapping
    public Result<Void> save(@RequestBody Budget budget) {
        boolean success = budgetService.save(budget);
        return success ? Result.success() : Result.error("新增失败");
    }

    @PutMapping
    public Result<Void> update(@RequestBody Budget budget) {
        boolean success = budgetService.update(budget);
        return success ? Result.success() : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = budgetService.delete(id);
        return success ? Result.success() : Result.error("删除失败");
    }
}

