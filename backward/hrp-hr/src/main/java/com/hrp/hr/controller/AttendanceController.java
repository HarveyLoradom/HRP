package com.hrp.hr.controller;

import com.hrp.common.entity.Result;
import com.hrp.hr.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hr/attendance")
@CrossOrigin
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @GetMapping("/my-list")
    public Result<List<?>> getMyList(
            @RequestParam(required = false) Long empId,
            @RequestParam(required = false) String month) {
        return Result.success(attendanceService.getMyList(empId, month));
    }
}

