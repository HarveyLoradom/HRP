package com.hrp.cost.controller;

import com.hrp.common.entity.CostReport;
import com.hrp.common.entity.Result;
import com.hrp.cost.service.CostReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cost/report")
@CrossOrigin
public class CostReportController {

    @Autowired
    private CostReportService costReportService;

    @GetMapping("/list")
    public Result<List<CostReport>> getAll() {
        List<CostReport> reports = costReportService.getAll();
        return Result.success(reports);
    }

    @GetMapping("/period/{reportPeriod}")
    public Result<List<CostReport>> getByPeriod(@PathVariable String reportPeriod) {
        List<CostReport> reports = costReportService.getByPeriod(reportPeriod);
        return Result.success(reports);
    }

    @GetMapping("/{id}")
    public Result<CostReport> getById(@PathVariable Long id) {
        CostReport report = costReportService.getById(id);
        return Result.success(report);
    }

    @PostMapping
    public Result<Void> save(@RequestBody CostReport costReport) {
        boolean success = costReportService.save(costReport);
        return success ? Result.success() : Result.error("保存失败");
    }

    @PutMapping
    public Result<Void> update(@RequestBody CostReport costReport) {
        boolean success = costReportService.update(costReport);
        return success ? Result.success() : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = costReportService.delete(id);
        return success ? Result.success() : Result.error("删除失败");
    }
}

