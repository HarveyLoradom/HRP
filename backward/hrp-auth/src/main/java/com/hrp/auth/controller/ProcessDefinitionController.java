package com.hrp.auth.controller;

import com.hrp.auth.service.ProcessDefinitionService;
import com.hrp.common.entity.ProcessDefinition;
import com.hrp.common.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 流程定义管理控制器
 */
@RestController
@RequestMapping("/auth/process-definition")
@CrossOrigin
public class ProcessDefinitionController {

    @Autowired
    private ProcessDefinitionService processDefinitionService;

    /**
     * 根据ID查询流程定义
     */
    @GetMapping("/{id}")
    public Result<ProcessDefinition> getById(@PathVariable Long id) {
        ProcessDefinition definition = processDefinitionService.getById(id);
        if (definition != null) {
            return Result.success(definition);
        }
        return Result.error("流程定义不存在");
    }

    /**
     * 根据KEY查询流程定义
     */
    @GetMapping("/key/{key}")
    public Result<ProcessDefinition> getByKey(@PathVariable String key) {
        ProcessDefinition definition = processDefinitionService.getByKey(key);
        if (definition != null) {
            return Result.success(definition);
        }
        return Result.error("流程定义不存在");
    }

    /**
     * 根据类型查询流程定义列表
     */
    @GetMapping("/type/{type}")
    public Result<List<ProcessDefinition>> getByType(@PathVariable String type) {
        List<ProcessDefinition> list = processDefinitionService.getByType(type);
        return Result.success(list);
    }

    /**
     * 查询所有流程定义
     */
    @GetMapping("/list")
    public Result<List<ProcessDefinition>> getAll() {
        List<ProcessDefinition> list = processDefinitionService.getAll();
        return Result.success(list);
    }

    /**
     * 新增流程定义
     */
    @PostMapping("/save")
    public Result<String> save(@RequestBody ProcessDefinition definition) {
        boolean success = processDefinitionService.save(definition);
        if (success) {
            return Result.success("新增成功");
        }
        return Result.error("新增失败");
    }

    /**
     * 更新流程定义
     */
    @PutMapping("/update")
    public Result<String> update(@RequestBody ProcessDefinition definition) {
        if (definition.getDefinitionId() == null) {
            return Result.error("流程定义ID不能为空");
        }
        boolean success = processDefinitionService.update(definition);
        if (success) {
            return Result.success("更新成功");
        }
        return Result.error("更新失败");
    }

    /**
     * 删除流程定义
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean success = processDefinitionService.delete(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }
}



