package com.hrp.budg.controller;

import com.hrp.common.entity.BudgetItem;
import com.hrp.common.entity.BudgetSubject;
import com.hrp.common.entity.PageResult;
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

    /**
     * 分页查询项目预算
     */
    @GetMapping("/page")
    public Result<PageResult<BudgetItem>> getPage(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String budgetYear,
            @RequestParam(required = false) String categoryType,
            @RequestParam(required = false) Long level1CategoryId,
            @RequestParam(required = false) Long level2CategoryId,
            @RequestParam(required = false) String itemName) {
        PageResult<BudgetItem> result = budgetItemService.getPage(page, size, budgetYear, categoryType,
                level1CategoryId, level2CategoryId, itemName);
        return Result.success(result);
    }

    @PostMapping
    public Result<Void> save(@RequestBody BudgetItem budgetItem) {
        try {
            boolean success = budgetItemService.save(budgetItem);
            return success ? Result.success() : Result.error("新增失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping
    public Result<Void> update(@RequestBody BudgetItem budgetItem) {
        boolean success = budgetItemService.update(budgetItem);
        return success ? Result.success() : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        try {
            boolean success = budgetItemService.delete(id);
            return success ? Result.success() : Result.error("删除失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 停用项目
     */
    @PostMapping("/stop/{id}")
    public Result<Void> stop(@PathVariable Long id) {
        boolean success = budgetItemService.stop(id);
        return success ? Result.success() : Result.error("停用失败");
    }

    /**
     * 启用项目
     */
    @PostMapping("/start/{id}")
    public Result<Void> start(@PathVariable Long id) {
        boolean success = budgetItemService.start(id);
        return success ? Result.success() : Result.error("启用失败");
    }

    /**
     * 分配主体
     */
    @PostMapping("/{itemId}/assign")
    public Result<Void> assignSubjects(@PathVariable Long itemId, @RequestBody List<Long> subjectIds) {
        try {
            boolean success = budgetItemService.assignSubjects(itemId, subjectIds);
            return success ? Result.success() : Result.error("分配失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取项目分配的主体列表
     */
    @GetMapping("/{itemId}/subjects")
    public Result<List<BudgetSubject>> getAssignedSubjects(@PathVariable Long itemId) {
        List<BudgetSubject> subjects = budgetItemService.getAssignedSubjects(itemId);
        return Result.success(subjects);
    }
}













