package com.hrp.efficiency.controller;

import com.hrp.common.entity.Equipment;
import com.hrp.common.entity.Result;
import com.hrp.common.exception.BusinessException;
import com.hrp.efficiency.service.EquipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/efficiency/equipment")
@CrossOrigin
public class EquipmentController {

    @Autowired
    private EquipmentService equipmentService;

    @GetMapping("/list")
    public Result<List<Equipment>> getAll() {
        List<Equipment> equipments = equipmentService.getAll();
        return Result.success(equipments);
    }

    @GetMapping("/status/{status}")
    public Result<List<Equipment>> getByStatus(@PathVariable String status) {
        List<Equipment> equipments = equipmentService.getByStatus(status);
        return Result.success(equipments);
    }

    @GetMapping("/{id}")
    public Result<Equipment> getById(@PathVariable Long id) {
        Equipment equipment = equipmentService.getById(id);
        return Result.success(equipment);
    }

    @PostMapping
    public Result<Void> save(@RequestBody Equipment equipment) {
        boolean success = equipmentService.save(equipment);
        if (!success) {
            throw new BusinessException("保存失败");
        }
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody Equipment equipment) {
        boolean success = equipmentService.update(equipment);
        if (!success) {
            throw new BusinessException("更新失败");
        }
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = equipmentService.delete(id);
        if (!success) {
            throw new BusinessException("删除失败");
        }
        return Result.success();
    }
}

