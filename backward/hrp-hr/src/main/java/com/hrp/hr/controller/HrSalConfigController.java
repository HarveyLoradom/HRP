package com.hrp.hr.controller;

import com.hrp.common.entity.HrSalConfig;
import com.hrp.common.entity.PageResult;
import com.hrp.common.entity.Result;
import com.hrp.hr.service.HrSalConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hr/salary/config")
@CrossOrigin
public class HrSalConfigController {

    @Autowired
    private HrSalConfigService hrSalConfigService;

    /**
     * 分页查询薪酬配置
     */
    @GetMapping("/page")
    public Result<PageResult<HrSalConfig>> getPage(
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size,
            @RequestParam(value = "empId", required = false) Long empId,
            @RequestParam(value = "empCode", required = false) String empCode,
            @RequestParam(value = "empName", required = false) String empName) {
        PageResult<HrSalConfig> pageResult = hrSalConfigService.getPage(page, size, empId, empCode, empName);
        return Result.success(pageResult);
    }

    /**
     * 根据ID查询薪酬配置详情
     */
    @GetMapping("/{id}")
    public Result<HrSalConfig> getById(@PathVariable("id") Integer id) {
        HrSalConfig config = hrSalConfigService.getById(id);
        if (config == null) {
            return Result.error("薪酬配置不存在");
        }
        return Result.success(config);
    }

    /**
     * 根据员工ID查询薪酬配置
     */
    @GetMapping("/emp/{empId}")
    public Result<HrSalConfig> getByEmpId(@PathVariable("empId") Long empId) {
        HrSalConfig config = hrSalConfigService.getByEmpId(empId);
        if (config == null) {
            return Result.error("该员工暂无薪酬配置");
        }
        return Result.success(config);
    }

    /**
     * 保存薪酬配置
     */
    @PostMapping
    public Result<HrSalConfig> save(@RequestBody HrSalConfig config) {
        try {
            HrSalConfig saved = hrSalConfigService.save(config);
            return Result.success(saved);
        } catch (Exception e) {
            return Result.error("保存失败：" + e.getMessage());
        }
    }

    /**
     * 更新薪酬配置
     */
    @PutMapping
    public Result<HrSalConfig> update(@RequestBody HrSalConfig config) {
        try {
            HrSalConfig updated = hrSalConfigService.update(config);
            return Result.success(updated);
        } catch (Exception e) {
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 删除薪酬配置
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable("id") Integer id) {
        boolean deleted = hrSalConfigService.delete(id);
        if (deleted) {
            return Result.success(true);
        } else {
            return Result.error("删除失败");
        }
    }

    /**
     * 获取所有已有薪酬配置的员工ID列表
     */
    @GetMapping("/emp-ids")
    public Result<List<Long>> getAllEmpIds() {
        List<Long> empIds = hrSalConfigService.getAllEmpIds();
        return Result.success(empIds);
    }

    /**
     * 批量创建薪酬配置
     */
    @PostMapping("/batch-create")
    public Result<String> batchCreate(@RequestBody java.util.Map<String, Object> body) {
        Object empIdsObj = body.get("empIds");
        if (empIdsObj == null) {
            return Result.error("请选择至少一个员工");
        }
        
        List<Long> empIds = new java.util.ArrayList<>();
        if (empIdsObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> empIdList = (List<Object>) empIdsObj;
            for (Object obj : empIdList) {
                if (obj instanceof Number) {
                    empIds.add(((Number) obj).longValue());
                }
            }
        }
        
        if (empIds.isEmpty()) {
            return Result.error("请选择至少一个员工");
        }
        
        String createUser = (String) body.get("createUser");
        return hrSalConfigService.batchCreate(empIds, createUser);
    }
}
