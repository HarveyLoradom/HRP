package com.hrp.efficiency.controller;

import com.hrp.common.entity.Result;
import com.hrp.common.exception.BusinessException;
import com.hrp.efficiency.service.InvestmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/efficiency/investment")
@CrossOrigin
public class InvestmentController {

    @Autowired
    private InvestmentService investmentService;

    @GetMapping("/list")
    public Result<List<?>> getAll() {
        return Result.success(investmentService.getAll());
    }

    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        return Result.success(investmentService.getById(id));
    }

    @PostMapping
    public Result<Void> save(@RequestBody Map<String, Object> data) {
        boolean success = investmentService.save(data);
        if (!success) {
            throw new BusinessException("保存失败");
        }
        return Result.success();
    }

    @PostMapping("/calculate")
    public Result<Map<String, Object>> calculate(@RequestBody Map<String, Object> data) {
        return Result.success(investmentService.calculate(data));
    }
}

