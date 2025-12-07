package com.hrp.hr.controller;

import com.hrp.common.entity.Result;
import com.hrp.common.entity.Salary;
import com.hrp.hr.service.SalaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hr/salary")
@CrossOrigin
public class SalaryController {

    @Autowired
    private SalaryService salaryService;

    @GetMapping("/list")
    public Result<List<Salary>> getAll() {
        List<Salary> salaries = salaryService.getAll();
        return Result.success(salaries);
    }

    @GetMapping("/emp/{empId}")
    public Result<List<Salary>> getByEmpId(@PathVariable Long empId) {
        List<Salary> salaries = salaryService.getByEmpId(empId);
        return Result.success(salaries);
    }

    @GetMapping("/month/{salaryMonth}")
    public Result<List<Salary>> getByMonth(@PathVariable String salaryMonth) {
        List<Salary> salaries = salaryService.getByMonth(salaryMonth);
        return Result.success(salaries);
    }

    @GetMapping("/{id}")
    public Result<Salary> getById(@PathVariable Long id) {
        Salary salary = salaryService.getById(id);
        return Result.success(salary);
    }

    @PostMapping
    public Result<Void> save(@RequestBody Salary salary) {
        boolean success = salaryService.save(salary);
        return success ? Result.success() : Result.error("保存失败");
    }

    @PutMapping
    public Result<Void> update(@RequestBody Salary salary) {
        boolean success = salaryService.update(salary);
        return success ? Result.success() : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = salaryService.delete(id);
        return success ? Result.success() : Result.error("删除失败");
    }

    @PostMapping("/{empId}/calculate/{salaryMonth}")
    public Result<Void> calculateSalary(@PathVariable Long empId, @PathVariable String salaryMonth) {
        boolean success = salaryService.calculateSalary(empId, salaryMonth);
        return success ? Result.success() : Result.error("计算失败");
    }
}

