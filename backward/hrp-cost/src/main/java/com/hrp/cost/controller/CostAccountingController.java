package com.hrp.cost.controller;

import com.hrp.common.entity.CostAccounting;
import com.hrp.common.entity.Result;
import com.hrp.cost.service.CostAccountingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping
    public Result<Void> save(@RequestBody CostAccounting costAccounting) {
        boolean success = costAccountingService.save(costAccounting);
        return success ? Result.success() : Result.error("保存失败");
    }

    @PutMapping
    public Result<Void> update(@RequestBody CostAccounting costAccounting) {
        boolean success = costAccountingService.update(costAccounting);
        return success ? Result.success() : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = costAccountingService.delete(id);
        return success ? Result.success() : Result.error("删除失败");
    }
}

