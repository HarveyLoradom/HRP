package com.hrp.user.controller;

import com.hrp.common.entity.Dept;
import com.hrp.common.entity.Result;
import com.hrp.user.service.DeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门管理控制器
 */
@RestController
@RequestMapping("/dept")
@CrossOrigin
public class DeptController {

    @Autowired
    private DeptService deptService;

    /**
     * 根据ID查询部门
     */
    @GetMapping("/{deptId}")
    public Result<Dept> getById(@PathVariable Long deptId) {
        Dept dept = deptService.getById(deptId);
        if (dept != null) {
            return Result.success(dept);
        }
        return Result.error("部门不存在");
    }

    /**
     * 查询所有部门
     */
    @GetMapping("/list")
    public Result<List<Dept>> getAll() {
        List<Dept> list = deptService.getAll();
        return Result.success(list);
    }

    /**
     * 根据上级部门编码查询部门列表
     */
    @GetMapping("/parent/{superDeptCode}")
    public Result<List<Dept>> getBySuperDeptCode(@PathVariable String superDeptCode) {
        List<Dept> list = deptService.getBySuperDeptCode(superDeptCode);
        return Result.success(list);
    }

    /**
     * 新增部门
     */
    @PostMapping("/save")
    public Result<String> save(@RequestBody Dept dept) {
        // 检查编码是否已存在
        Dept existDept = deptService.getByCode(dept.getDeptCode());
        if (existDept != null) {
            return Result.error("部门编码已存在");
        }
        boolean success = deptService.save(dept);
        if (success) {
            return Result.success("新增成功");
        }
        return Result.error("新增失败");
    }

    /**
     * 更新部门
     */
    @PutMapping("/update")
    public Result<String> update(@RequestBody Dept dept) {
        if (dept.getDeptId() == null) {
            return Result.error("部门ID不能为空");
        }
        boolean success = deptService.update(dept);
        if (success) {
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    /**
     * 删除部门
     */
    @DeleteMapping("/{deptId}")
    public Result<String> delete(@PathVariable Long deptId) {
        boolean success = deptService.delete(deptId);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }
}
