package com.hrp.budg.controller;

import com.hrp.common.entity.BudgetDetail;
import com.hrp.common.entity.BudgetExecutionDetail;
import com.hrp.common.entity.BudgetApplyDetail;
import com.hrp.common.entity.BudgetDetailRecord;
import com.hrp.common.entity.Result;
import com.hrp.common.entity.PageResult;
import com.hrp.budg.service.BudgetDetailService;
import com.hrp.budg.service.BudgetDetailRecordService;
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
    
    @Autowired
    private BudgetDetailRecordService budgetDetailRecordService;

    @GetMapping("/list")
    public Result<PageResult<BudgetDetail>> getList(
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size,
            @RequestParam(value = "subjectId", required = false) Long subjectId,
            @RequestParam(value = "budgetYear", required = false) String budgetYear,
            @RequestParam(value = "categoryType", required = false) String categoryType,
            @RequestParam(value = "itemName", required = false) String itemName) {
        PageResult<BudgetDetail> pageResult = budgetDetailService.getPage(page, size, subjectId, budgetYear, categoryType, itemName);
        return Result.success(pageResult);
    }

    @GetMapping("/execution")
    public Result<List<BudgetExecutionDetail>> getExecutionDetails(
            @RequestParam(value = "itemId") Long itemId,
            @RequestParam(value = "subjectId") Long subjectId) {
        List<BudgetExecutionDetail> details = budgetDetailService.getExecutionDetails(itemId, subjectId);
        return Result.success(details);
    }

    @GetMapping("/applies")
    public Result<List<BudgetDetailRecord>> getAppliesBySubjectAndItem(
            @RequestParam(value = "subjectCode") String subjectCode,
            @RequestParam(value = "itemCode") String itemCode) {
        List<BudgetDetailRecord> records = budgetDetailRecordService.getAppliesBySubjectAndItem(subjectCode, itemCode);
        return Result.success(records);
    }

    @GetMapping("/apply")
    public Result<List<BudgetApplyDetail>> getApplyDetails(
            @RequestParam(value = "itemId") Long itemId,
            @RequestParam(value = "subjectId") Long subjectId) {
        List<BudgetApplyDetail> details = budgetDetailService.getApplyDetails(itemId, subjectId);
        return Result.success(details);
    }

    @GetMapping("/apply-execution")
    public Result<List<BudgetExecutionDetail>> getApplyExecutionDetails(
            @RequestParam(value = "itemId") Long itemId,
            @RequestParam(value = "subjectId") Long subjectId,
            @RequestParam(value = "applyNo") String applyNo) {
        List<BudgetExecutionDetail> details = budgetDetailService.getApplyExecutionDetails(itemId, subjectId, applyNo);
        return Result.success(details);
    }
    
    /**
     * 根据业务单号查询预算明细记录
     */
    @GetMapping("/business-no/{businessNo}")
    public Result<List<BudgetDetailRecord>> getByBusinessNo(@PathVariable("businessNo") String businessNo) {
        List<BudgetDetailRecord> records = budgetDetailRecordService.getByBusinessNo(businessNo);
        return Result.success(records);
    }
    
    /**
     * 批量保存预算明细记录
     */
    @PostMapping("/record/batch")
    public Result<Boolean> saveBatch(@RequestBody List<BudgetDetailRecord> records) {
        try {
            boolean success = budgetDetailRecordService.saveBatch(records);
            return Result.success(success);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("保存预算明细记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据业务单号取消预算明细记录
     */
    @PostMapping("/record/cancel/{businessNo}")
    public Result<Boolean> cancelByBusinessNo(@PathVariable("businessNo") String businessNo) {
        boolean success = budgetDetailRecordService.cancelByBusinessNo(businessNo);
        return Result.success(success);
    }
    
    /**
     * 根据业务ID取消预算明细记录
     */
    @PostMapping("/record/cancel-by-id/{businessId}")
    public Result<Boolean> cancelByBusinessId(@PathVariable("businessId") Long businessId) {
        boolean success = budgetDetailRecordService.cancelByBusinessId(businessId);
        return Result.success(success);
    }
    
    /**
     * 批量更新预算明细记录（根据detailId更新）
     */
    @PutMapping("/record/batch")
    public Result<Boolean> updateBatch(@RequestBody List<BudgetDetailRecord> records) {
        boolean success = budgetDetailRecordService.updateBatch(records);
        return Result.success(success);
    }
}

