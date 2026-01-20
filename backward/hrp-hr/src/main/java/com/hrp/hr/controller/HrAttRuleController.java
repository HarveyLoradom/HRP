package com.hrp.hr.controller;

import com.hrp.common.entity.HrAttRule;
import com.hrp.common.entity.Result;
import com.hrp.hr.service.HrAttRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hr/attendance/rule")
@CrossOrigin
public class HrAttRuleController {

    @Autowired
    private HrAttRuleService hrAttRuleService;

    /**
     * 查询所有规则
     */
    @GetMapping("/list")
    public Result<List<HrAttRule>> getAll() {
        List<HrAttRule> list = hrAttRuleService.getAll();
        return Result.success(list);
    }

    /**
     * 根据规则类型查询
     */
    @GetMapping("/type/{ruleType}")
    public Result<List<HrAttRule>> getByRuleType(@PathVariable("ruleType") String ruleType) {
        List<HrAttRule> list = hrAttRuleService.getByRuleType(ruleType);
        return Result.success(list);
    }

    /**
     * 根据ID查询规则详情
     */
    @GetMapping("/{id}")
    public Result<HrAttRule> getById(@PathVariable("id") Integer id) {
        HrAttRule rule = hrAttRuleService.getById(id);
        if (rule == null) {
            return Result.error("规则不存在");
        }
        return Result.success(rule);
    }

    /**
     * 保存规则
     */
    @PostMapping
    public Result<HrAttRule> save(@RequestBody HrAttRule rule) {
        try {
            HrAttRule saved = hrAttRuleService.save(rule);
            return Result.success(saved);
        } catch (Exception e) {
            return Result.error("保存失败：" + e.getMessage());
        }
    }

    /**
     * 更新规则
     */
    @PutMapping
    public Result<HrAttRule> update(@RequestBody HrAttRule rule) {
        try {
            HrAttRule updated = hrAttRuleService.update(rule);
            return Result.success(updated);
        } catch (Exception e) {
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 删除规则
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable("id") Integer id) {
        boolean deleted = hrAttRuleService.delete(id);
        if (deleted) {
            return Result.success(true);
        } else {
            return Result.error("删除失败");
        }
    }
}

