package com.hrp.user.controller;

import com.hrp.common.entity.Employee;
import com.hrp.common.entity.Result;
import com.hrp.user.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 职工管理控制器
 */
@RestController
@RequestMapping("/employee")
@CrossOrigin
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 根据ID查询职工
     */
    @GetMapping("/{empId}")
    public Result<Employee> getById(@PathVariable Long empId) {
        Employee employee = employeeService.getById(empId);
        if (employee != null) {
            return Result.success(employee);
        }
        return Result.error("职工不存在");
    }

    /**
     * 根据工号查询职工（用于个人中心）
     */
    @GetMapping("/code/{empCode}")
    public Result<Employee> getByCode(@PathVariable String empCode) {
        Employee employee = employeeService.getByCode(empCode);
        if (employee != null) {
            return Result.success(employee);
        }
        return Result.error("职工不存在");
    }

    /**
     * 查询所有职工
     */
    @GetMapping("/list")
    public Result<List<Employee>> getAll() {
        List<Employee> list = employeeService.getAll();
        return Result.success(list);
    }

    /**
     * 根据部门ID查询职工列表
     */
    @GetMapping("/dept/{deptId}")
    public Result<List<Employee>> getByDeptId(@PathVariable Long deptId) {
        List<Employee> list = employeeService.getByDeptId(deptId);
        return Result.success(list);
    }

    /**
     * 新增职工
     */
    @PostMapping("/save")
    public Result<String> save(@RequestBody Employee employee) {
        // 检查编码是否已存在
        Employee existEmployee = employeeService.getByCode(employee.getEmpCode());
        if (existEmployee != null) {
            return Result.error("职工编码已存在");
        }
        boolean success = employeeService.save(employee);
        if (success) {
            return Result.success("新增成功");
        }
        return Result.error("新增失败");
    }

    /**
     * 更新职工
     */
    @PutMapping("/update")
    public Result<String> update(@RequestBody Employee employee) {
        if (employee.getEmpId() == null) {
            return Result.error("职工ID不能为空");
        }
        boolean success = employeeService.update(employee);
        if (success) {
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    /**
     * 删除职工
     */
    @DeleteMapping("/{empId}")
    public Result<String> delete(@PathVariable Long empId) {
        boolean success = employeeService.delete(empId);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }
}
