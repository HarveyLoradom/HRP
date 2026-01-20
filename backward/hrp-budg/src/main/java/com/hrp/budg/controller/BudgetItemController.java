package com.hrp.budg.controller;

import com.hrp.common.entity.BudgetItem;
import com.hrp.common.entity.BudgetSubject;
import com.hrp.common.entity.PageResult;
import com.hrp.common.entity.Result;
import com.hrp.common.util.JwtUtil;
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
    public Result<BudgetItem> getById(@PathVariable("id") Long id) {
        BudgetItem item = budgetItemService.getById(id);
        return Result.success(item);
    }

    @GetMapping("/code/{code}")
    public Result<BudgetItem> getByCode(@PathVariable("code") String code) {
        BudgetItem item = budgetItemService.getByCode(code);
        return Result.success(item);
    }

    @GetMapping("/list")
    public Result<List<BudgetItem>> getAll() {
        List<BudgetItem> list = budgetItemService.getAll();
        return Result.success(list);
    }

    @GetMapping("/category/{categoryId}")
    public Result<List<BudgetItem>> getByCategoryId(@PathVariable("categoryId") Long categoryId) {
        List<BudgetItem> list = budgetItemService.getByCategoryId(categoryId);
        return Result.success(list);
    }

    /**
     * 分页查询项目预算
     */
    @GetMapping("/page")
    public Result<PageResult<BudgetItem>> getPage(
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size,
            @RequestParam(value = "budgetYear", required = false) String budgetYear,
            @RequestParam(value = "categoryType", required = false) String categoryType,
            @RequestParam(value = "level1CategoryId", required = false) Long level1CategoryId,
            @RequestParam(value = "level2CategoryId", required = false) Long level2CategoryId,
            @RequestParam(value = "itemName", required = false) String itemName,
            @RequestParam(value = "isStop", required = false) Long isStop) {
        PageResult<BudgetItem> result = budgetItemService.getPage(page, size, budgetYear, categoryType,
                level1CategoryId, level2CategoryId, itemName, isStop);
        return Result.success(result);
    }

    @PostMapping
    public Result<Void> save(@RequestBody BudgetItem budgetItem, 
                             @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 从token中获取当前用户账号作为createUser
            String createUser = getCurrentUserAccount(token);
            if (createUser != null && !createUser.isEmpty()) {
                budgetItem.setCreateUser(createUser);
            } else {
                budgetItem.setCreateUser("SYSTEM");
            }
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
    public Result<Void> stop(@PathVariable("id") Long id) {
        boolean success = budgetItemService.stop(id);
        return success ? Result.success() : Result.error("停用失败");
    }

    /**
     * 启用项目
     */
    @PostMapping("/start/{id}")
    public Result<Void> start(@PathVariable("id") Long id) {
        boolean success = budgetItemService.start(id);
        return success ? Result.success() : Result.error("启用失败");
    }

    /**
     * 分配主体
     */
    @PostMapping("/{itemId}/assign")
    public Result<Void> assignSubjects(@PathVariable("itemId") Long itemId, 
                                       @RequestBody List<Long> subjectIds,
                                       @RequestHeader(value = "Authorization", required = false) String token) {
        try {
            // 从token中获取当前用户账号作为createUser
            String createUser = getCurrentUserAccount(token);
            if (createUser == null || createUser.isEmpty()) {
                createUser = "SYSTEM";
            }
            boolean success = budgetItemService.assignSubjects(itemId, subjectIds, createUser);
            return success ? Result.success() : Result.error("分配失败");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取项目分配的主体列表
     */
    @GetMapping("/{itemId}/subjects")
    public Result<List<BudgetSubject>> getAssignedSubjects(@PathVariable("itemId") Long itemId) {
        List<BudgetSubject> subjects = budgetItemService.getAssignedSubjects(itemId);
        return Result.success(subjects);
    }

    /**
     * 获取主体分配的项目列表
     */
    @GetMapping("/subject/{subjectId}/items")
    public Result<List<BudgetItem>> getAssignedItems(@PathVariable("subjectId") Long subjectId) {
        List<BudgetItem> items = budgetItemService.getAssignedItems(subjectId);
        return Result.success(items);
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













