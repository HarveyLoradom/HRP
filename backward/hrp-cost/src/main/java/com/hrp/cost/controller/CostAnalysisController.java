package com.hrp.cost.controller;

import com.hrp.common.entity.CostAnalysis;
import com.hrp.common.entity.Result;
import com.hrp.cost.service.CostAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cost/analysis")
@CrossOrigin
public class CostAnalysisController {

    @Autowired
    private CostAnalysisService costAnalysisService;

    @GetMapping("/list")
    public Result<List<CostAnalysis>> getAll() {
        List<CostAnalysis> analyses = costAnalysisService.getAll();
        return Result.success(analyses);
    }

    @GetMapping("/{id}")
    public Result<CostAnalysis> getById(@PathVariable Long id) {
        CostAnalysis analysis = costAnalysisService.getById(id);
        return Result.success(analysis);
    }

    @PostMapping
    public Result<Void> save(@RequestBody CostAnalysis costAnalysis) {
        boolean success = costAnalysisService.save(costAnalysis);
        return success ? Result.success() : Result.error("保存失败");
    }

    @PutMapping
    public Result<Void> update(@RequestBody CostAnalysis costAnalysis) {
        boolean success = costAnalysisService.update(costAnalysis);
        return success ? Result.success() : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = costAnalysisService.delete(id);
        return success ? Result.success() : Result.error("删除失败");
    }
}

