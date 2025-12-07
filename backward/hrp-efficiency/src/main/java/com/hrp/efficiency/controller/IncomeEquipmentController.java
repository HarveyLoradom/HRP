package com.hrp.efficiency.controller;

import com.hrp.common.entity.IncomeEquipment;
import com.hrp.common.entity.Result;
import com.hrp.efficiency.service.IncomeEquipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/efficiency/income/equipment")
@CrossOrigin
public class IncomeEquipmentController {

    @Autowired
    private IncomeEquipmentService incomeEquipmentService;

    @GetMapping("/list")
    public Result<List<IncomeEquipment>> getAll() {
        List<IncomeEquipment> incomes = incomeEquipmentService.getAll();
        return Result.success(incomes);
    }

    @GetMapping("/equipment/{equipmentId}")
    public Result<List<IncomeEquipment>> getByEquipmentId(@PathVariable Long equipmentId) {
        List<IncomeEquipment> incomes = incomeEquipmentService.getByEquipmentId(equipmentId);
        return Result.success(incomes);
    }

    @PostMapping("/range")
    public Result<List<IncomeEquipment>> getByDateRange(@RequestBody Map<String, String> params) {
        LocalDate startDate = LocalDate.parse(params.get("startDate"));
        LocalDate endDate = LocalDate.parse(params.get("endDate"));
        List<IncomeEquipment> incomes = incomeEquipmentService.getByDateRange(startDate, endDate);
        return Result.success(incomes);
    }

    @GetMapping("/{id}")
    public Result<IncomeEquipment> getById(@PathVariable Long id) {
        IncomeEquipment income = incomeEquipmentService.getById(id);
        return Result.success(income);
    }

    @PostMapping
    public Result<Void> save(@RequestBody IncomeEquipment incomeEquipment) {
        boolean success = incomeEquipmentService.save(incomeEquipment);
        return success ? Result.success() : Result.error("保存失败");
    }

    @PutMapping
    public Result<Void> update(@RequestBody IncomeEquipment incomeEquipment) {
        boolean success = incomeEquipmentService.update(incomeEquipment);
        return success ? Result.success() : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = incomeEquipmentService.delete(id);
        return success ? Result.success() : Result.error("删除失败");
    }
}

