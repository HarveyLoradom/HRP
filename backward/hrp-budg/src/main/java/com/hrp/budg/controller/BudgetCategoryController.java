package com.hrp.budg.controller;

import com.hrp.common.entity.BudgetCategory;
import com.hrp.common.entity.Result;
import com.hrp.common.entity.PageResult;
import com.hrp.budg.service.BudgetCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 预算分类管理控制器
 */
@RestController
@RequestMapping("/budg/category")
@CrossOrigin
public class BudgetCategoryController {

    @Autowired
    private BudgetCategoryService budgetCategoryService;

    /**
     * 分页查询一级分类
     */
    @GetMapping("/level1/page")
    public Result<PageResult<BudgetCategory>> getLevel1Page(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String budgetYear,
            @RequestParam(required = false) String categoryType) {
        PageResult<BudgetCategory> pageResult = budgetCategoryService.getLevel1Page(page, size, budgetYear, categoryType);
        return Result.success(pageResult);
    }

    /**
     * 分页查询二级分类
     */
    @GetMapping("/level2/page")
    public Result<PageResult<BudgetCategory>> getLevel2Page(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String budgetYear,
            @RequestParam(required = false) String categoryType,
            @RequestParam(required = false) Long parentCategoryId) {
        PageResult<BudgetCategory> pageResult = budgetCategoryService.getLevel2Page(page, size, budgetYear, categoryType, parentCategoryId);
        return Result.success(pageResult);
    }

    /**
     * 查询所有一级分类（用于下拉选择）
     */
    @GetMapping("/level1/list")
    public Result<java.util.List<BudgetCategory>> getLevel1List(
            @RequestParam(required = false) String budgetYear,
            @RequestParam(required = false) String categoryType) {
        java.util.List<BudgetCategory> list = budgetCategoryService.getLevel1List(budgetYear, categoryType);
        return Result.success(list);
    }

    /**
     * 查询所有二级分类（用于下拉选择）
     */
    @GetMapping("/level2/list")
    public Result<java.util.List<BudgetCategory>> getLevel2List(
            @RequestParam(required = false) String budgetYear,
            @RequestParam(required = false) String categoryType,
            @RequestParam(required = false) Long parentCategoryId) {
        java.util.List<BudgetCategory> list = budgetCategoryService.getLevel2List(budgetYear, categoryType, parentCategoryId);
        return Result.success(list);
    }

    /**
     * 根据ID查询分类
     */
    @GetMapping("/{id}")
    public Result<BudgetCategory> getById(@PathVariable Long id) {
        BudgetCategory category = budgetCategoryService.getById(id);
        return Result.success(category);
    }

    /**
     * 新增一级分类
     */
    @PostMapping("/level1")
    public Result<Void> saveLevel1(@RequestBody BudgetCategory category) {
        category.setCategoryLevel(1);
        boolean success = budgetCategoryService.save(category);
        return success ? Result.success() : Result.error("新增失败");
    }

    /**
     * 新增二级分类
     */
    @PostMapping("/level2")
    public Result<Void> saveLevel2(@RequestBody BudgetCategory category) {
        category.setCategoryLevel(2);
        boolean success = budgetCategoryService.save(category);
        return success ? Result.success() : Result.error("新增失败");
    }

    /**
     * 更新分类
     */
    @PutMapping
    public Result<Void> update(@RequestBody BudgetCategory category) {
        boolean success = budgetCategoryService.update(category);
        return success ? Result.success() : Result.error("更新失败");
    }

    /**
     * 删除分类（物理删除）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = budgetCategoryService.delete(id);
        return success ? Result.success() : Result.error("删除失败");
    }

    /**
     * 停用分类
     */
    @PostMapping("/stop/{id}")
    public Result<Void> stop(@PathVariable Long id) {
        boolean success = budgetCategoryService.stop(id);
        return success ? Result.success() : Result.error("停用失败");
    }

    /**
     * 启用分类
     */
    @PostMapping("/start/{id}")
    public Result<Void> start(@PathVariable Long id) {
        boolean success = budgetCategoryService.start(id);
        return success ? Result.success() : Result.error("启用失败");
    }
}

