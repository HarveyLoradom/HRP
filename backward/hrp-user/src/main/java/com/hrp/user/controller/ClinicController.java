package com.hrp.user.controller;

import com.hrp.common.entity.Clinic;
import com.hrp.common.entity.Result;
import com.hrp.user.service.ClinicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 科室管理控制器
 */
@RestController
@RequestMapping("/clinic")
@CrossOrigin
public class ClinicController {

    @Autowired
    private ClinicService clinicService;

    /**
     * 根据ID查询科室
     */
    @GetMapping("/{clinicId}")
    public Result<Clinic> getById(@PathVariable Long clinicId) {
        Clinic clinic = clinicService.getById(clinicId);
        if (clinic != null) {
            return Result.success(clinic);
        }
        return Result.error("科室不存在");
    }

    /**
     * 查询所有科室
     */
    @GetMapping("/list")
    public Result<List<Clinic>> getAll() {
        List<Clinic> list = clinicService.getAll();
        return Result.success(list);
    }

    /**
     * 根据部门ID查询科室列表
     */
    @GetMapping("/dept/{deptId}")
    public Result<List<Clinic>> getByDeptId(@PathVariable Long deptId) {
        List<Clinic> list = clinicService.getByDeptId(deptId);
        return Result.success(list);
    }

    /**
     * 新增科室
     */
    @PostMapping("/save")
    public Result<String> save(@RequestBody Clinic clinic) {
        // 检查编码是否已存在
        Clinic existClinic = clinicService.getByCode(clinic.getClinicCode());
        if (existClinic != null) {
            return Result.error("科室编码已存在");
        }
        boolean success = clinicService.save(clinic);
        if (success) {
            return Result.success("新增成功");
        }
        return Result.error("新增失败");
    }

    /**
     * 更新科室
     */
    @PutMapping("/update")
    public Result<String> update(@RequestBody Clinic clinic) {
        if (clinic.getClinicId() == null) {
            return Result.error("科室ID不能为空");
        }
        boolean success = clinicService.update(clinic);
        if (success) {
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    /**
     * 删除科室
     */
    @DeleteMapping("/{clinicId}")
    public Result<String> delete(@PathVariable Long clinicId) {
        boolean success = clinicService.delete(clinicId);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }
}
