package com.hrp.budg.controller;

import com.hrp.budg.service.BudgetAdjustmentService;
import com.hrp.budg.service.impl.BudgetAdjustmentServiceImpl;
import com.hrp.common.entity.BudgetAdjustment;
import com.hrp.common.entity.PageResult;
import com.hrp.common.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 预算调整控制器
 */
@RestController
@RequestMapping("/budg/adjustment")
@CrossOrigin
public class BudgetAdjustmentController {

    @Autowired
    private BudgetAdjustmentService budgetAdjustmentService;

    @Autowired
    private BudgetAdjustmentServiceImpl budgetAdjustmentServiceImpl;

    @GetMapping("/{id}")
    public Result<BudgetAdjustment> getById(@PathVariable("id") Long id) {
        BudgetAdjustment adjustment = budgetAdjustmentService.getById(id);
        return Result.success(adjustment);
    }

    @GetMapping("/no/{no}")
    public Result<BudgetAdjustment> getByNo(@PathVariable("no") String no) {
        BudgetAdjustment adjustment = budgetAdjustmentService.getByNo(no);
        return Result.success(adjustment);
    }

    @GetMapping("/page")
    public Result<PageResult<BudgetAdjustment>> getPage(
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size,
            @RequestParam(value = "adjustmentType", required = false) String adjustmentType,
            @RequestParam(value = "subjectId", required = false) Long subjectId,
            @RequestParam(value = "itemId", required = false) Long itemId,
            @RequestParam(value = "adjustmentNo", required = false) String adjustmentNo) {
        Map<String, Object> params = new HashMap<>();
        if (adjustmentType != null && !adjustmentType.isEmpty()) {
            params.put("adjustmentType", adjustmentType);
        }
        if (subjectId != null) {
            params.put("subjectId", subjectId);
        }
        if (itemId != null) {
            params.put("itemId", itemId);
        }
        if (adjustmentNo != null && !adjustmentNo.isEmpty()) {
            params.put("adjustmentNo", adjustmentNo);
        }
        PageResult<BudgetAdjustment> pageResult = budgetAdjustmentService.getPage(page, size, params);
        return Result.success(pageResult);
    }

    @PostMapping
    public Result<Void> save(@RequestBody BudgetAdjustment budgetAdjustment) {
        boolean success = budgetAdjustmentService.save(budgetAdjustment);
        if (success) {
            return Result.success(null);
        } else {
            return Result.error("保存失败");
        }
    }

    @PutMapping
    public Result<Void> update(@RequestBody BudgetAdjustment budgetAdjustment) {
        boolean success = budgetAdjustmentService.update(budgetAdjustment);
        if (success) {
            return Result.success(null);
        } else {
            return Result.error("更新失败");
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        try {
            boolean success = budgetAdjustmentService.delete(id);
            if (success) {
                return Result.success(null);
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    @PostMapping("/submit")
    public Result<Void> submit(@RequestBody BudgetAdjustment budgetAdjustment) {
        try {
            boolean success = budgetAdjustmentServiceImpl.submit(budgetAdjustment);
            if (success) {
                return Result.success(null);
            } else {
                return Result.error("提交失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("提交失败: " + e.getMessage());
        }
    }

    @PostMapping("/save-and-submit")
    public Result<Void> saveAndSubmit(@RequestBody BudgetAdjustment budgetAdjustment) {
        try {
            boolean success = budgetAdjustmentServiceImpl.saveAndSubmit(budgetAdjustment);
            if (success) {
                return Result.success(null);
            } else {
                return Result.error("保存并提交失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("保存并提交失败: " + e.getMessage());
        }
    }
}

