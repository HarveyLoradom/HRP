package com.hrp.budg.controller;

import com.hrp.common.entity.BudgetDetail;
import com.hrp.common.entity.BudgetExecutionDetail;
import com.hrp.common.entity.BudgetApplyDetail;
import com.hrp.common.entity.Result;
import com.hrp.common.entity.PageResult;
import com.hrp.budg.service.BudgetDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 预算明细控制器
 */
@RestController
@RequestMapping("/budg/detail")
@CrossOrigin
public class BudgetDetailController {

    @Autowired
    private BudgetDetailService budgetDetailService;

    @GetMapping("/list")
    public Result<PageResult<BudgetDetail>> getList(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) String budgetYear,
            @RequestParam(required = false) String categoryType,
            @RequestParam(required = false) String itemName) {
        PageResult<BudgetDetail> pageResult = budgetDetailService.getPage(page, size, deptId, budgetYear, categoryType, itemName);
        return Result.success(pageResult);
    }

    @GetMapping("/execution")
    public Result<List<BudgetExecutionDetail>> getExecutionDetails(
            @RequestParam Long itemId,
            @RequestParam Long subjectId) {
        List<BudgetExecutionDetail> details = budgetDetailService.getExecutionDetails(itemId, subjectId);
        return Result.success(details);
    }

    @GetMapping("/apply")
    public Result<List<BudgetApplyDetail>> getApplyDetails(
            @RequestParam Long itemId,
            @RequestParam Long subjectId) {
        List<BudgetApplyDetail> details = budgetDetailService.getApplyDetails(itemId, subjectId);
        return Result.success(details);
    }
}

