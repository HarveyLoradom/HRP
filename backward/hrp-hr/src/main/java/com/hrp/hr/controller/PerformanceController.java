package com.hrp.hr.controller;

import com.hrp.common.entity.Result;
import com.hrp.hr.service.PerformanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hr/performance")
@CrossOrigin
public class PerformanceController {

    @Autowired
    private PerformanceService performanceService;

    @GetMapping("/my-list")
    public Result<List<?>> getMyList(
            @RequestParam(required = false) Long empId,
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String quarter) {
        return Result.success(performanceService.getMyList(empId, year, quarter));
    }
}

