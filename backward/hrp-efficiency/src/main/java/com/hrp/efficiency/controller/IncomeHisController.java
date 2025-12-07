package com.hrp.efficiency.controller;

import com.hrp.common.entity.IncomeHis;
import com.hrp.common.entity.Result;
import com.hrp.efficiency.service.IncomeHisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/efficiency/income/his")
@CrossOrigin
public class IncomeHisController {

    @Autowired
    private IncomeHisService incomeHisService;

    @GetMapping("/list")
    public Result<List<IncomeHis>> getAll() {
        List<IncomeHis> incomes = incomeHisService.getAll();
        return Result.success(incomes);
    }

    @PostMapping("/range")
    public Result<List<IncomeHis>> getByDateRange(@RequestBody Map<String, String> params) {
        LocalDate startDate = LocalDate.parse(params.get("startDate"));
        LocalDate endDate = LocalDate.parse(params.get("endDate"));
        List<IncomeHis> incomes = incomeHisService.getByDateRange(startDate, endDate);
        return Result.success(incomes);
    }

    @GetMapping("/{id}")
    public Result<IncomeHis> getById(@PathVariable Long id) {
        IncomeHis income = incomeHisService.getById(id);
        return Result.success(income);
    }

    @PostMapping
    public Result<Void> save(@RequestBody IncomeHis incomeHis) {
        boolean success = incomeHisService.save(incomeHis);
        return success ? Result.success() : Result.error("保存失败");
    }

    @PutMapping
    public Result<Void> update(@RequestBody IncomeHis incomeHis) {
        boolean success = incomeHisService.update(incomeHis);
        return success ? Result.success() : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = incomeHisService.delete(id);
        return success ? Result.success() : Result.error("删除失败");
    }
}

