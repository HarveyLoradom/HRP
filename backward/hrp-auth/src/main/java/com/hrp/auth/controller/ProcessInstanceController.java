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
     * 分页查询所有流程实例
     */
    @GetMapping("/page")
    public Result<com.hrp.common.entity.PageResult<ProcessInstance>> getAllPage(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size) {
        com.hrp.common.entity.PageResult<ProcessInstance> pageResult = processInstanceService.getAllPage(page, size);
        return Result.success(pageResult);
    }

    /**
     * 根据状态分页查询流程实例
     */
    @GetMapping("/status/{status}/page")
    public Result<com.hrp.common.entity.PageResult<ProcessInstance>> getByStatusPage(
            @PathVariable String status,
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size) {
        com.hrp.common.entity.PageResult<ProcessInstance> pageResult = processInstanceService.getByStatusPage(status, page, size);
        return Result.success(pageResult);
    }

    /**
     * 根据流程实例ID查询变量列表
     */
    @GetMapping("/{id}/variables")
    public Result<List<ProcessVariable>> getVariables(@PathVariable Long id) {
        List<ProcessVariable> variables = processInstanceService.getVariablesByInstanceId(id);
        return Result.success(variables);
    }

    /**
     * 启动流程实例
     */
    @PostMapping("/start")
    public Result<ProcessInstance> startProcess(@RequestBody StartProcessRequest request) {
        try {
            ProcessInstance instance = processInstanceService.startProcess(
                request.getProcessDefinitionId(),
                request.getBusinessKey(),
                request.getBusinessType(),
                request.getBusinessId()
            );
            if (instance != null) {
                return Result.success(instance);
            }
            return Result.error("启动流程失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("启动流程失败：" + e.getMessage());
        }
    }

    /**
     * 启动流程请求对象
     */
    public static class StartProcessRequest {
        private Long processDefinitionId;
        private String businessKey;
        private String businessType;
        private Long businessId;

        public Long getProcessDefinitionId() { return processDefinitionId; }
        public void setProcessDefinitionId(Long processDefinitionId) { this.processDefinitionId = processDefinitionId; }
        public String getBusinessKey() { return businessKey; }
        public void setBusinessKey(String businessKey) { this.businessKey = businessKey; }
        public String getBusinessType() { return businessType; }
        public void setBusinessType(String businessType) { this.businessType = businessType; }
        public Long getBusinessId() { return businessId; }
        public void setBusinessId(Long businessId) { this.businessId = businessId; }
    }
}



