package com.hrp.hr.controller;

import com.hrp.common.entity.HrAttRecord;
import com.hrp.common.entity.PageResult;
import com.hrp.common.entity.Result;
import com.hrp.hr.service.HrAttRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/hr/attendance/record")
@CrossOrigin
public class HrAttRecordController {

    @Autowired
    private HrAttRecordService hrAttRecordService;

    /**
     * 分页查询考勤记录
     */
    @GetMapping("/page")
    public Result<PageResult<HrAttRecord>> getPage(
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size,
            @RequestParam(value = "empId", required = false) Long empId,
            @RequestParam(value = "attDate", required = false) String attDate,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "attStatus", required = false) String attStatus,
            @RequestParam(value = "attType", required = false) String attType,
            @RequestParam(value = "attSubType", required = false) String attSubType) {
        PageResult<HrAttRecord> pageResult = hrAttRecordService.getPage(
                page, size, empId, attDate, startDate, endDate, attStatus, attType, attSubType);
        return Result.success(pageResult);
    }

    /**
     * 根据ID查询考勤记录详情
     */
    @GetMapping("/{id}")
    public Result<HrAttRecord> getById(@PathVariable("id") Long id) {
        HrAttRecord record = hrAttRecordService.getById(id);
        if (record == null) {
            return Result.error("考勤记录不存在");
        }
        return Result.success(record);
    }

    /**
     * 保存考勤记录
     */
    @PostMapping
    public Result<HrAttRecord> save(@RequestBody HrAttRecord record) {
        try {
            HrAttRecord saved = hrAttRecordService.save(record);
            return Result.success(saved);
        } catch (Exception e) {
            return Result.error("保存失败：" + e.getMessage());
        }
    }

    /**
     * 更新考勤记录
     */
    @PutMapping
    public Result<HrAttRecord> update(@RequestBody HrAttRecord record) {
        try {
            HrAttRecord updated = hrAttRecordService.update(record);
            return Result.success(updated);
        } catch (Exception e) {
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 删除考勤记录
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable("id") Long id) {
        boolean deleted = hrAttRecordService.delete(id);
        if (deleted) {
            return Result.success(true);
        } else {
            return Result.error("删除失败");
        }
    }

    /**
     * 查询今日打卡记录
     */
    @GetMapping("/today")
    public Result<HrAttRecord> getTodayRecord(
            @RequestParam("empId") Long empId) {
        LocalDate today = LocalDate.now();
        String attDateStr = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        HrAttRecord record = hrAttRecordService.getTodayRecord(empId, attDateStr);
        return Result.success(record);
    }

    /**
     * 上班打卡
     */
    @PostMapping("/clock-in")
    public Result<HrAttRecord> clockIn(@RequestParam("empId") Long empId) {
        try {
            HrAttRecord record = hrAttRecordService.clockIn(empId);
            return Result.success(record);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 下班打卡
     */
    @PostMapping("/clock-out")
    public Result<HrAttRecord> clockOut(@RequestParam("empId") Long empId) {
        try {
            HrAttRecord record = hrAttRecordService.clockOut(empId);
            return Result.success(record);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
