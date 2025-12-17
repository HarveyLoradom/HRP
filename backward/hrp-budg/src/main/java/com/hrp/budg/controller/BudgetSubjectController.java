package com.hrp.budg.controller;

import com.hrp.common.entity.BudgetSubject;
import com.hrp.common.entity.Result;
import com.hrp.budg.service.BudgetSubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 预算主体管理控制器
 */
@RestController
@RequestMapping("/budg/subject")
@CrossOrigin
public class BudgetSubjectController {

    @Autowired
    private BudgetSubjectService budgetSubjectService;

    @GetMapping("/{id}")
    public Result<BudgetSubject> getById(@PathVariable Long id) {
        BudgetSubject subject = budgetSubjectService.getById(id);
        return Result.success(subject);
    }

    @GetMapping("/code/{code}")
    public Result<BudgetSubject> getByCode(@PathVariable String code) {
        BudgetSubject subject = budgetSubjectService.getByCode(code);
        return Result.success(subject);
    }

    @GetMapping("/list")
    public Result<List<BudgetSubject>> getAll() {
        List<BudgetSubject> list = budgetSubjectService.getAll();
        return Result.success(list);
    }

    @GetMapping("/tree")
    public Result<List<BudgetSubject>> getTree() {
        List<BudgetSubject> list = budgetSubjectService.getTree();
        return Result.success(list);
    }

    @GetMapping("/parent/{parentId}")
    public Result<List<BudgetSubject>> getByParentId(@PathVariable Long parentId) {
        List<BudgetSubject> list = budgetSubjectService.getByParentId(parentId);
        return Result.success(list);
    }

    @GetMapping("/dept/{deptId}")
    public Result<List<BudgetSubject>> getByDeptId(@PathVariable Long deptId) {
        List<BudgetSubject> list = budgetSubjectService.getByDeptId(deptId);
        return Result.success(list);
    }

    @PostMapping
    public Result<Void> save(@RequestBody BudgetSubject budgetSubject) {
        boolean success = budgetSubjectService.save(budgetSubject);
        return success ? Result.success() : Result.error("新增失败");
    }

    @PutMapping
    public Result<Void> update(@RequestBody BudgetSubject budgetSubject) {
        boolean success = budgetSubjectService.update(budgetSubject);
        return success ? Result.success() : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean success = budgetSubjectService.delete(id);
        return success ? Result.success() : Result.error("删除失败");
    }

    @PostMapping("/stop/{id}")
    public Result<Void> stop(@PathVariable Long id) {
        boolean success = budgetSubjectService.stop(id);
        return success ? Result.success() : Result.error("停用失败");
    }

    @PostMapping("/start/{id}")
    public Result<Void> start(@PathVariable Long id) {
        boolean success = budgetSubjectService.start(id);
        return success ? Result.success() : Result.error("启用失败");
    }

    @GetMapping("/related-depts/{subjectId}")
    public Result<List<com.hrp.common.entity.Dept>> getRelatedDepts(@PathVariable Long subjectId) {
        List<com.hrp.common.entity.Dept> depts = budgetSubjectService.getRelatedDepts(subjectId);
        return Result.success(depts);
    }
}













