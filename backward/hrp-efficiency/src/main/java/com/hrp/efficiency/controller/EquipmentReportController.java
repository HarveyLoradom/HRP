package com.hrp.efficiency.controller;

import com.hrp.common.entity.Result;
import com.hrp.common.exception.BusinessException;
import com.hrp.efficiency.service.EquipmentReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/efficiency/equipment/report")
@CrossOrigin
public class EquipmentReportController {

    @Autowired
    private EquipmentReportService equipmentReportService;

    @GetMapping("/list")
    public Result<List<?>> getAll() {
        return Result.success(equipmentReportService.getAll());
    }

    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        return Result.success(equipmentReportService.getById(id));
    }

    @PostMapping("/generate")
    public Result<?> generate(@RequestBody Map<String, Object> data) {
        Object report = equipmentReportService.generate(data);
        if (report == null) {
            throw new BusinessException("生成失败");
        }
        return Result.success(report);
    }
}

