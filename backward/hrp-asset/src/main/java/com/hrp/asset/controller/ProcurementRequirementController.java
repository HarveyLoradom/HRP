package com.hrp.asset.controller;

import com.hrp.common.entity.ProcurementRequirement;
import com.hrp.common.entity.Result;
import com.hrp.asset.service.ProcurementRequirementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/asset/procurement")
@CrossOrigin
public class ProcurementRequirementController {

    @Autowired
    private ProcurementRequirementService procurementRequirementService;

    @GetMapping("/list")
    public Result<List<ProcurementRequirement>> getAll() {
        List<ProcurementRequirement> requirements = procurementRequirementService.getAll();
        return Result.success(requirements);
    }

    @GetMapping("/status/{status}")
    public Result<List<ProcurementRequirement>> getByStatus(@PathVariable String status) {
        List<ProcurementRequirement> requirements = procurementRequirementService.getByStatus(status);
        return Result.success(requirements);
    }

    @GetMapping("/{id}")
    public Result<ProcurementRequirement> getById(@PathVariable Long id) {
        ProcurementRequirement requirement = procurementRequirementService.getById(id);
        return Result.success(requirement);
    }

    @PostMapping
    public Result<Void> save(@RequestBody ProcurementRequirement requirement) {
        boolean success = procurementRequirementService.save(requirement);
        return success ? Result.success() : Result.error("保存失败");
    }

    @PutMapping
    public Result<Void> update(@RequestBody ProcurementRequirement requirement) {
        boolean success = procurementRequirementService.update(requirement);
        return success ? Result.success() : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = procurementRequirementService.delete(id);
        return success ? Result.success() : Result.error("删除失败");
    }
}

