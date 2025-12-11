package com.hrp.auth.controller;

import com.hrp.auth.service.ProcessInstanceService;
import com.hrp.common.entity.ProcessInstance;
import com.hrp.common.entity.ProcessVariable;
import com.hrp.common.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 流程实例管理控制器
 */
@RestController
@RequestMapping("/auth/process-instance")
@CrossOrigin
public class ProcessInstanceController {

    @Autowired
    private ProcessInstanceService processInstanceService;

    /**
     * 根据ID查询流程实例
     */
    @GetMapping("/{id}")
    public Result<ProcessInstance> getById(@PathVariable Long id) {
        ProcessInstance instance = processInstanceService.getById(id);
        if (instance != null) {
            return Result.success(instance);
        }
        return Result.error("流程实例不存在");
    }

    /**
     * 根据业务主键查询流程实例
     */
    @GetMapping("/business-key/{businessKey}")
    public Result<ProcessInstance> getByBusinessKey(@PathVariable String businessKey) {
        ProcessInstance instance = processInstanceService.getByBusinessKey(businessKey);
        if (instance != null) {
            return Result.success(instance);
        }
        return Result.error("流程实例不存在");
    }

    /**
     * 查询所有流程实例
     */
    @GetMapping("/list")
    public Result<List<ProcessInstance>> getAll() {
        List<ProcessInstance> list = processInstanceService.getAll();
        return Result.success(list);
    }

    /**
     * 根据状态查询流程实例
     */
    @GetMapping("/status/{status}")
    public Result<List<ProcessInstance>> getByStatus(@PathVariable String status) {
        List<ProcessInstance> list = processInstanceService.getByStatus(status);
        return Result.success(list);
    }

    /**
     * 根据流程实例ID查询变量列表
     */
    @GetMapping("/{id}/variables")
    public Result<List<ProcessVariable>> getVariables(@PathVariable Long id) {
        List<ProcessVariable> variables = processInstanceService.getVariablesByInstanceId(id);
        return Result.success(variables);
    }
}



