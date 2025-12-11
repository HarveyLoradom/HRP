package com.hrp.auth.controller;

import com.hrp.auth.service.ProcessTaskService;
import com.hrp.common.entity.ProcessTask;
import com.hrp.common.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 流程任务管理控制器
 */
@RestController
@RequestMapping("/auth/process-task")
@CrossOrigin
public class ProcessTaskController {

    @Autowired
    private ProcessTaskService processTaskService;

    /**
     * 根据ID查询流程任务
     */
    @GetMapping("/{id}")
    public Result<ProcessTask> getById(@PathVariable Long id) {
        ProcessTask task = processTaskService.getById(id);
        if (task != null) {
            return Result.success(task);
        }
        return Result.error("流程任务不存在");
    }

    /**
     * 根据流程实例ID查询任务列表
     */
    @GetMapping("/instance/{instanceId}")
    public Result<List<ProcessTask>> getByInstanceId(@PathVariable Long instanceId) {
        List<ProcessTask> list = processTaskService.getByInstanceId(instanceId);
        return Result.success(list);
    }

    /**
     * 根据办理人ID查询任务列表
     */
    @GetMapping("/assignee/{userId}")
    public Result<List<ProcessTask>> getByAssignee(@PathVariable String userId) {
        List<ProcessTask> list = processTaskService.getByAssignee(userId);
        return Result.success(list);
    }

    /**
     * 根据业务主键查询任务列表
     */
    @GetMapping("/business-key/{businessKey}")
    public Result<List<ProcessTask>> getByBusinessKey(@PathVariable String businessKey) {
        List<ProcessTask> list = processTaskService.getByBusinessKey(businessKey);
        return Result.success(list);
    }

    /**
     * 根据任务状态查询任务列表
     */
    @GetMapping("/status/{status}")
    public Result<List<ProcessTask>> getByStatus(@PathVariable String status) {
        List<ProcessTask> list = processTaskService.getByStatus(status);
        return Result.success(list);
    }

    /**
     * 转办任务（更改办理人）
     */
    @PutMapping("/transfer")
    public Result<String> transferTask(@RequestBody TransferTaskRequest request) {
        boolean success = processTaskService.transferTask(
            request.getTaskId(),
            request.getNewAssigneeUserId(),
            request.getNewAssigneeUserName(),
            request.getNewAssigneeEmpCode()
        );
        if (success) {
            return Result.success("转办成功");
        }
        return Result.error("转办失败");
    }

    /**
     * 转办任务请求对象
     */
    public static class TransferTaskRequest {
        private Long taskId;
        private String newAssigneeUserId;
        private String newAssigneeUserName;
        private String newAssigneeEmpCode;

        public Long getTaskId() {
            return taskId;
        }

        public void setTaskId(Long taskId) {
            this.taskId = taskId;
        }

        public String getNewAssigneeUserId() {
            return newAssigneeUserId;
        }

        public void setNewAssigneeUserId(String newAssigneeUserId) {
            this.newAssigneeUserId = newAssigneeUserId;
        }

        public String getNewAssigneeUserName() {
            return newAssigneeUserName;
        }

        public void setNewAssigneeUserName(String newAssigneeUserName) {
            this.newAssigneeUserName = newAssigneeUserName;
        }

        public String getNewAssigneeEmpCode() {
            return newAssigneeEmpCode;
        }

        public void setNewAssigneeEmpCode(String newAssigneeEmpCode) {
            this.newAssigneeEmpCode = newAssigneeEmpCode;
        }
    }
}



