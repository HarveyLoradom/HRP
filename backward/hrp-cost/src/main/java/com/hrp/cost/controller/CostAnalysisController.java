package com.hrp.cost.controller;

import com.hrp.common.entity.CostAnalysis;
import com.hrp.common.entity.Result;
import com.hrp.common.exception.BusinessException;
import com.hrp.cost.service.CostAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @GetMapping("/detail/{id}")
    public Result<Map<String, Object>> getDetail(@PathVariable Long id) {
        return Result.success(costAnalysisService.getDetail(id));
    }

    @PostMapping
    public Result<Void> save(@RequestBody CostAnalysis costAnalysis) {
        boolean success = costAnalysisService.save(costAnalysis);
        if (!success) {
            throw new BusinessException("保存失败");
        }
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody CostAnalysis costAnalysis) {
        boolean success = costAnalysisService.update(costAnalysis);
        if (!success) {
            throw new BusinessException("更新失败");
        }
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = costAnalysisService.delete(id);
        if (!success) {
            throw new BusinessException("删除失败");
        }
        return Result.success();
    }

    @PostMapping("/compare")
    public Result<List<Map<String, Object>>> compare(@RequestBody Map<String, String> params) {
        return Result.success(costAnalysisService.compare(params.get("period1"), params.get("period2")));
    }
}

