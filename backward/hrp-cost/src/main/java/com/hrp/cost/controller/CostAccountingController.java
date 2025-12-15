package com.hrp.cost.controller;

import com.hrp.common.entity.CostAccounting;
import com.hrp.common.entity.Result;
import com.hrp.common.exception.BusinessException;
import com.hrp.cost.service.CostAccountingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cost/accounting")
@CrossOrigin
public class CostAccountingController {

    @Autowired
    private CostAccountingService costAccountingService;

    @GetMapping("/list")
    public Result<List<CostAccounting>> getAll() {
        List<CostAccounting> accountings = costAccountingService.getAll();
        return Result.success(accountings);
    }

    @GetMapping("/period/{accountingPeriod}")
    public Result<List<CostAccounting>> getByPeriod(@PathVariable String accountingPeriod) {
        List<CostAccounting> accountings = costAccountingService.getByPeriod(accountingPeriod);
        return Result.success(accountings);
    }

    @GetMapping("/{id}")
    public Result<CostAccounting> getById(@PathVariable Long id) {
        CostAccounting accounting = costAccountingService.getById(id);
        return Result.success(accounting);
    }

    @GetMapping("/detail/{id}")
    public Result<Map<String, Object>> getDetail(@PathVariable Long id) {
        return Result.success(costAccountingService.getDetail(id));
    }

    @PostMapping
    public Result<Void> save(@RequestBody CostAccounting costAccounting) {
        boolean success = costAccountingService.save(costAccounting);
        if (!success) {
            throw new BusinessException("保存失败");
        }
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody CostAccounting costAccounting) {
        boolean success = costAccountingService.update(costAccounting);
        if (!success) {
            throw new BusinessException("更新失败");
        }
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = costAccountingService.delete(id);
        if (!success) {
            throw new BusinessException("删除失败");
        }
        return Result.success();
    }

    @PostMapping("/calculate")
    public Result<Void> calculate(@RequestBody Map<String, Object> params) {
        Long accountingId = Long.valueOf(params.get("accountingId").toString());
        boolean success = costAccountingService.calculate(accountingId);
        if (!success) {
            throw new BusinessException("核算失败");
        }
        return Result.success();
    }
}

