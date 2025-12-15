package com.hrp.efficiency.controller;

import com.hrp.common.entity.Result;
import com.hrp.efficiency.service.DataCollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/efficiency/collection")
@CrossOrigin
public class DataCollectionController {

    @Autowired
    private DataCollectionService dataCollectionService;

    @PostMapping("/dept")
    public Result<Void> collectDeptData(@RequestBody Map<String, Object> data) {
        boolean success = dataCollectionService.collectDeptData(data);
        return success ? Result.success() : Result.error("采集失败");
    }

    @PostMapping("/equipment")
    public Result<Void> collectEquipmentData(@RequestBody Map<String, Object> data) {
        boolean success = dataCollectionService.collectEquipmentData(data);
        return success ? Result.success() : Result.error("采集失败");
    }

    @GetMapping("/records")
    public Result<List<?>> getCollectionRecords(@RequestParam(required = false) String collectionType) {
        return Result.success(dataCollectionService.getCollectionRecords(collectionType));
    }
}

