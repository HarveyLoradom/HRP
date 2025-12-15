package com.hrp.efficiency.controller;

import com.hrp.common.entity.CostData;
import com.hrp.common.entity.Result;
import com.hrp.common.exception.BusinessException;
import com.hrp.efficiency.service.CostDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/efficiency/cost")
@CrossOrigin
public class CostDataController {

    @Autowired
    private CostDataService costDataService;

    @GetMapping("/list")
    public Result<List<CostData>> getAll() {
        List<CostData> costs = costDataService.getAll();
        return Result.success(costs);
    }

    @PostMapping("/range")
    public Result<List<CostData>> getByDateRange(@RequestBody Map<String, String> params) {
        LocalDate startDate = LocalDate.parse(params.get("startDate"));
        LocalDate endDate = LocalDate.parse(params.get("endDate"));
        List<CostData> costs = costDataService.getByDateRange(startDate, endDate);
        return Result.success(costs);
    }

    @GetMapping("/{id}")
    public Result<CostData> getById(@PathVariable Long id) {
        CostData cost = costDataService.getById(id);
        return Result.success(cost);
    }

    @PostMapping
    public Result<Void> save(@RequestBody CostData costData) {
        boolean success = costDataService.save(costData);
        if (!success) {
            throw new BusinessException("保存失败");
        }
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody CostData costData) {
        boolean success = costDataService.update(costData);
        if (!success) {
            throw new BusinessException("更新失败");
        }
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = costDataService.delete(id);
        if (!success) {
            throw new BusinessException("删除失败");
        }
        return Result.success();
    }
}

