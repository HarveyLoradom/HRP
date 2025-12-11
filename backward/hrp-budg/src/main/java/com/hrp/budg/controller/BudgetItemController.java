package com.hrp.budg.controller;

import com.hrp.common.entity.BudgetItem;
import com.hrp.common.entity.Result;
import com.hrp.budg.service.BudgetItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 预算项目管理控制器
 */
@RestController
@RequestMapping("/budg/item")
@CrossOrigin
public class BudgetItemController {

    @Autowired
    private BudgetItemService budgetItemService;

    @GetMapping("/{id}")
    public Result<BudgetItem> getById(@PathVariable Long id) {
        BudgetItem item = budgetItemService.getById(id);
        return Result.success(item);
    }

    @GetMapping("/code/{code}")
    public Result<BudgetItem> getByCode(@PathVariable String code) {
        BudgetItem item = budgetItemService.getByCode(code);
        return Result.success(item);
    }

    @GetMapping("/list")
    public Result<List<BudgetItem>> getAll() {
        List<BudgetItem> list = budgetItemService.getAll();
        return Result.success(list);
    }

    @GetMapping("/category/{categoryId}")
    public Result<List<BudgetItem>> getByCategoryId(@PathVariable Long categoryId) {
        List<BudgetItem> list = budgetItemService.getByCategoryId(categoryId);
        return Result.success(list);
    }

    @PostMapping
    public Result<Void> save(@RequestBody BudgetItem budgetItem) {
        boolean success = budgetItemService.save(budgetItem);
        return success ? Result.success() : Result.error("新增失败");
    }

    @PutMapping
    public Result<Void> update(@RequestBody BudgetItem budgetItem) {
        boolean success = budgetItemService.update(budgetItem);
        return success ? Result.success() : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = budgetItemService.delete(id);
        return success ? Result.success() : Result.error("删除失败");
    }
}




