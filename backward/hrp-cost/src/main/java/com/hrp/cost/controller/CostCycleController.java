package com.hrp.cost.controller;

import com.hrp.common.entity.CostCycle;
import com.hrp.common.entity.Result;
import com.hrp.cost.service.CostCycleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 成本周期控制器
 */
@RestController
@RequestMapping("/cost/cycle")
@CrossOrigin
public class CostCycleController {

    @Autowired
    private CostCycleService costCycleService;

    @GetMapping("/list")
    public Result<List<CostCycle>> getList(
            @RequestParam(value = "cycleCode", required = false) String cycleCode,
            @RequestParam(value = "cycleName", required = false) String cycleName,
            @RequestParam(value = "cycleType", required = false) String cycleType,
            @RequestParam(value = "status", required = false) Integer status) {
        List<CostCycle> list = costCycleService.getAll(cycleCode, cycleName, cycleType, status);
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<CostCycle> getById(@PathVariable("id") Long id) {
        CostCycle costCycle = costCycleService.getById(id);
        return Result.success(costCycle);
    }

    @PostMapping
    public Result<CostCycle> save(@RequestBody CostCycle costCycle) {
        try {
            CostCycle saved = costCycleService.save(costCycle);
            return Result.success(saved);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping
    public Result<CostCycle> update(@RequestBody CostCycle costCycle) {
        try {
            CostCycle updated = costCycleService.update(costCycle);
            return Result.success(updated);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        boolean success = costCycleService.delete(id);
        if (success) {
            return Result.success();
        } else {
            return Result.error("删除失败");
        }
    }
}

