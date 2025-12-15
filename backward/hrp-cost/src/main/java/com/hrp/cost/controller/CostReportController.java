package com.hrp.cost.controller;

import com.hrp.common.entity.CostReport;
import com.hrp.common.entity.Result;
import com.hrp.common.exception.BusinessException;
import com.hrp.cost.service.CostReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
        if (!success) {
            throw new BusinessException("保存失败");
        }
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody CostReport costReport) {
        boolean success = costReportService.update(costReport);
        if (!success) {
            throw new BusinessException("更新失败");
        }
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = costReportService.delete(id);
        if (!success) {
            throw new BusinessException("删除失败");
        }
        return Result.success();
    }

    @PostMapping("/generate")
    public Result<CostReport> generate(@RequestBody Map<String, Object> params) {
        CostReport report = costReportService.generate(params);
        if (report == null) {
            throw new BusinessException("生成失败");
        }
        return Result.success(report);
    }

    @GetMapping("/{id}/chart")
    public Result<Map<String, Object>> getChart(@PathVariable Long id) {
        Map<String, Object> chartData = costReportService.getChartData(id);
        return Result.success(chartData);
    }
}

