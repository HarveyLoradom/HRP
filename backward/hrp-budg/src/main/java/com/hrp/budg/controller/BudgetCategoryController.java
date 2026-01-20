package com.hrp.budg.controller;

import com.hrp.common.entity.BudgetCategory;
import com.hrp.common.entity.Result;
import com.hrp.common.entity.PageResult;
import com.hrp.common.exception.BusinessException;
import com.hrp.common.util.JwtUtil;
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
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size,
            @RequestParam(value = "budgetYear", required = false) String budgetYear,
            @RequestParam(value = "categoryType", required = false) String categoryType,
            @RequestParam(value = "isStop", required = false) Long isStop) {
        PageResult<BudgetCategory> pageResult = budgetCategoryService.getLevel1Page(page, size, budgetYear, categoryType, isStop);
        return Result.success(pageResult);
    }

    /**
     * 分页查询二级分类
     */
    @GetMapping("/level2/page")
    public Result<PageResult<BudgetCategory>> getLevel2Page(
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size,
            @RequestParam(value = "budgetYear", required = false) String budgetYear,
            @RequestParam(value = "categoryType", required = false) String categoryType,
            @RequestParam(value = "parentCategoryId", required = false) Long parentCategoryId,
            @RequestParam(value = "isStop", required = false) Long isStop) {
        PageResult<BudgetCategory> pageResult = budgetCategoryService.getLevel2Page(page, size, budgetYear, categoryType, parentCategoryId, isStop);
        return Result.success(pageResult);
    }

    /**
     * 查询所有一级分类（用于下拉选择）
     */
    @GetMapping("/level1/list")
    public Result<java.util.List<BudgetCategory>> getLevel1List(
            @RequestParam(value = "budgetYear", required = false) String budgetYear,
            @RequestParam(value = "categoryType", required = false) String categoryType,
            @RequestParam(value = "isStop", required = false) Long isStop) {
        java.util.List<BudgetCategory> list = budgetCategoryService.getLevel1List(budgetYear, categoryType, isStop);
        return Result.success(list);
    }

    /**
     * 查询所有二级分类（用于下拉选择）
     */
    @GetMapping("/level2/list")
    public Result<java.util.List<BudgetCategory>> getLevel2List(
            @RequestParam(value = "budgetYear", required = false) String budgetYear,
            @RequestParam(value = "categoryType", required = false) String categoryType,
            @RequestParam(value = "parentCategoryId", required = false) Long parentCategoryId,
            @RequestParam(value = "isStop", required = false) Long isStop) {
        java.util.List<BudgetCategory> list = budgetCategoryService.getLevel2List(budgetYear, categoryType, parentCategoryId, isStop);
        return Result.success(list);
    }

    /**
     * 根据ID查询分类
     */
    /**
     * 查询所有分类（兼容旧接口）
     */
    @GetMapping("/list")
    public Result<java.util.List<BudgetCategory>> getAll() {
        // 返回所有一级和二级分类
        java.util.List<BudgetCategory> allCategories = new java.util.ArrayList<>();
        java.util.List<BudgetCategory> level1List = budgetCategoryService.getLevel1List(null, null, null);
        java.util.List<BudgetCategory> level2List = budgetCategoryService.getLevel2List(null, null, null, null);
        if (level1List != null) {
            allCategories.addAll(level1List);
        }
        if (level2List != null) {
            allCategories.addAll(level2List);
        }
        return Result.success(allCategories);
    }

    @GetMapping("/{id}")
    public Result<BudgetCategory> getById(@PathVariable("id") Long id) {
        BudgetCategory category = budgetCategoryService.getById(id);
        return Result.success(category);
    }

    /**
     * 新增一级分类
     */
    @PostMapping("/level1")
    public Result<Void> saveLevel1(@RequestBody BudgetCategory category,
                                    @RequestHeader(value = "Authorization", required = false) String token) {
        // 从token中获取当前用户账号作为createUser
        String createUser = getCurrentUserAccount(token);
        if (createUser != null && !createUser.isEmpty()) {
            category.setCreateUser(createUser);
        } else {
            category.setCreateUser("SYSTEM");
        }
        category.setCategoryLevel(1);
        boolean success = budgetCategoryService.save(category);
        return success ? Result.success() : Result.error("新增失败");
    }

    /**
     * 新增二级分类
     */
    @PostMapping("/level2")
    public Result<Void> saveLevel2(@RequestBody BudgetCategory category,
                                   @RequestHeader(value = "Authorization", required = false) String token) {
        // 从token中获取当前用户账号作为createUser
        String createUser = getCurrentUserAccount(token);
        if (createUser != null && !createUser.isEmpty()) {
            category.setCreateUser(createUser);
        } else {
            category.setCreateUser("SYSTEM");
        }
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
        try {
            boolean success = budgetCategoryService.delete(id);
            return success ? Result.success() : Result.error("删除失败");
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 停用分类
     */
    @PostMapping("/stop/{id}")
    public Result<Void> stop(@PathVariable("id") Long id) {
        try {
            boolean success = budgetCategoryService.stop(id);
            return success ? Result.success() : Result.error("停用失败");
        } catch (BusinessException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 启用分类
     */
    @PostMapping("/start/{id}")
    public Result<Void> start(@PathVariable("id") Long id) {
        boolean success = budgetCategoryService.start(id);
        return success ? Result.success() : Result.error("启用失败");
    }

    /**
     * 从token中获取当前用户账号
     */
    private String getCurrentUserAccount(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }
        try {
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            // 解析token获取账号
            String account = JwtUtil.getAccount(token);
            return account;
        } catch (Exception e) {
            return null;
        }
    }
}

