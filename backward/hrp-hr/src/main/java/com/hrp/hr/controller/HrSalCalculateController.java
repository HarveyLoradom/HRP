package com.hrp.hr.controller;

import com.hrp.common.entity.HrSalCalculate;
import com.hrp.common.entity.PageResult;
import com.hrp.common.entity.Result;
import com.hrp.hr.service.HrSalCalculateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/hr/salary/calculate")
@CrossOrigin
public class HrSalCalculateController {

    @Autowired
    private HrSalCalculateService hrSalCalculateService;

    /**
     * 分页查询薪酬核算
     */
    @GetMapping("/page")
    public Result<PageResult<HrSalCalculate>> getPage(
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size,
            @RequestParam(value = "empId", required = false) Long empId,
            @RequestParam(value = "empCode", required = false) String empCode,
            @RequestParam(value = "empName", required = false) String empName,
            @RequestParam(value = "calcMonth", required = false) String calcMonth,
            @RequestParam(value = "calcStatus", required = false) String calcStatus) {
        PageResult<HrSalCalculate> pageResult = hrSalCalculateService.getPage(
                page, size, empId, empCode, empName, calcMonth, calcStatus);
        return Result.success(pageResult);
    }

    /**
     * 根据ID查询薪酬核算详情
     */
    @GetMapping("/{id}")
    public Result<HrSalCalculate> getById(@PathVariable("id") Long id) {
        HrSalCalculate calculate = hrSalCalculateService.getById(id);
        if (calculate == null) {
            return Result.error("薪酬核算不存在");
        }
        return Result.success(calculate);
    }

    /**
     * 根据员工ID和月份查询薪酬核算
     */
    @GetMapping("/emp-month")
    public Result<HrSalCalculate> getByEmpIdAndMonth(
            @RequestParam(value = "empId") Long empId,
            @RequestParam(value = "calcMonth") String calcMonth) {
        HrSalCalculate calculate = hrSalCalculateService.getByEmpIdAndMonth(empId, calcMonth);
        if (calculate == null) {
            return Result.error("该员工在该月份的薪酬核算不存在");
        }
        return Result.success(calculate);
    }

    /**
     * 计算单个员工薪酬
     */
    @PostMapping
    public Result<HrSalCalculate> calculateSalary(@RequestParam(value = "empId") Long empId, @RequestParam(value = "calcMonth") String calcMonth) {
        try {
            HrSalCalculate calculate = hrSalCalculateService.calculateSalary(empId, calcMonth);
            return Result.success(calculate);
        } catch (Exception e) {
            return Result.error("计算失败：" + e.getMessage());
        }
    }

    /**
     * 批量计算薪酬
     */
    @PostMapping("/batch")
    public Result<List<HrSalCalculate>> batchCalculateSalary(@RequestBody Map<String, Object> params) {
        try {
            String calcMonth = (String) params.get("calcMonth");
            @SuppressWarnings("unchecked")
            List<Object> empIdsObj = (List<Object>) params.get("empIds");
            if (calcMonth == null || empIdsObj == null || empIdsObj.isEmpty()) {
                return Result.error("核算月份和员工ID列表不能为空");
            }
            
            // 将empIds转换为Long类型（支持Integer和Long两种类型）
            List<Long> empIds = new java.util.ArrayList<>();
            for (Object obj : empIdsObj) {
                if (obj instanceof Number) {
                    empIds.add(((Number) obj).longValue());
                } else if (obj instanceof String) {
                    empIds.add(Long.parseLong((String) obj));
                }
            }
            
            if (empIds.isEmpty()) {
                return Result.error("员工ID列表不能为空");
            }
            
            List<HrSalCalculate> results = hrSalCalculateService.batchCalculateSalary(calcMonth, empIds);
            return Result.success(results);
        } catch (Exception e) {
            return Result.error("批量计算失败：" + e.getMessage());
        }
    }

    /**
     * 更新薪酬核算
     */
    @PutMapping
    public Result<HrSalCalculate> update(@RequestBody HrSalCalculate calculate) {
        try {
            HrSalCalculate updated = hrSalCalculateService.update(calculate);
            return Result.success(updated);
        } catch (Exception e) {
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 删除薪酬核算
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable("id") Long id) {
        boolean deleted = hrSalCalculateService.delete(id);
        if (deleted) {
            return Result.success(true);
        } else {
            return Result.error("删除失败");
        }
    }

    /**
     * 发放薪酬（单个）
     */
    @PostMapping("/pay/{id}")
    public Result<Boolean> paySalary(@PathVariable("id") Long id) {
        try {
            boolean paid = hrSalCalculateService.paySalary(id);
            if (paid) {
                return Result.success(true);
            } else {
                return Result.error("发放失败");
            }
        } catch (Exception e) {
            return Result.error("发放失败：" + e.getMessage());
        }
    }

    /**
     * 批量发放薪酬
     */
    @PostMapping("/batch-pay")
    public Result<Boolean> batchPaySalary(@RequestBody Map<String, Object> params) {
        try {
            @SuppressWarnings("unchecked")
            List<Object> calcIdsObj = (List<Object>) params.get("calcIds");
            if (calcIdsObj == null || calcIdsObj.isEmpty()) {
                return Result.error("请选择至少一条记录");
            }
            
            // 将calcIds转换为Long类型（支持Integer和Long两种类型）
            List<Long> calcIds = new java.util.ArrayList<>();
            for (Object obj : calcIdsObj) {
                if (obj instanceof Number) {
                    calcIds.add(((Number) obj).longValue());
                } else if (obj instanceof String) {
                    calcIds.add(Long.parseLong((String) obj));
                }
            }
            
            if (calcIds.isEmpty()) {
                return Result.error("请选择至少一条记录");
            }
            
            boolean paid = hrSalCalculateService.batchPaySalary(calcIds);
            if (paid) {
                return Result.success(true);
            } else {
                return Result.error("批量发放失败");
            }
        } catch (Exception e) {
            return Result.error("批量发放失败：" + e.getMessage());
        }
    }
}
