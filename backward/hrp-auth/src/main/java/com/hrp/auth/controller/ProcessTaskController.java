package com.hrp.auth.controller;

import com.hrp.auth.service.ProcessTaskService;
import com.hrp.common.entity.ProcessInstance;
import com.hrp.common.entity.ProcessTask;
import com.hrp.common.entity.ProcessVariable;
import com.hrp.common.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    public Result<ProcessTask> getById(@PathVariable("id") String id) {
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
    public Result<List<ProcessTask>> getByInstanceId(@PathVariable("instanceId") Long instanceId) {
        List<ProcessTask> list = processTaskService.getByInstanceId(instanceId);
        return Result.success(list);
    }

    /**
     * 根据办理人ID查询任务列表
     */
    @GetMapping("/assignee/{userId}")
    public Result<List<ProcessTask>> getByAssignee(@PathVariable("userId") String userId) {
        List<ProcessTask> list = processTaskService.getByAssignee(userId);
        return Result.success(list);
    }
    
    /**
     * 根据办理人ID分页查询任务列表
     */
    @GetMapping("/assignee/{userId}/page")
    public Result<com.hrp.common.entity.PageResult<ProcessTask>> getByAssigneePage(
            @PathVariable("userId") String userId,
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size) {
        com.hrp.common.entity.PageResult<ProcessTask> pageResult = processTaskService.getByAssigneePage(userId, page, size);
        return Result.success(pageResult);
    }
    
    /**
     * 根据办理人ID和任务状态查询任务列表（用于查询已审批等历史记录）
     */
    @GetMapping("/assignee/{userId}/status/{taskStatus}")
    public Result<List<ProcessTask>> getByAssigneeAndStatus(@PathVariable("userId") String userId, @PathVariable("taskStatus") String taskStatus) {
        List<ProcessTask> list = processTaskService.getByAssigneeAndStatus(userId, taskStatus);
        return Result.success(list);
    }
    
    /**
     * 根据办理人ID和任务状态分页查询任务列表（用于查询已审批等历史记录）
     */
    @GetMapping("/assignee/{userId}/status/{taskStatus}/page")
    public Result<com.hrp.common.entity.PageResult<ProcessTask>> getByAssigneeAndStatusPage(
            @PathVariable("userId") String userId,
            @PathVariable("taskStatus") String taskStatus,
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size) {
        com.hrp.common.entity.PageResult<ProcessTask> pageResult = processTaskService.getByAssigneeAndStatusPage(userId, taskStatus, page, size);
        return Result.success(pageResult);
    }
    
    /**
     * 查询所有当前节点的任务（显示所有流程的当前审批节点）
     */
    @GetMapping("/current/all")
    public Result<List<ProcessTask>> getAllCurrentTasks() {
        List<ProcessTask> list = processTaskService.getAllCurrentTasks();
        return Result.success(list);
    }
    
    /**
     * 分页查询所有当前节点的任务（显示所有流程的当前审批节点）
     */
    @GetMapping("/current/all/page")
    public Result<com.hrp.common.entity.PageResult<ProcessTask>> getAllCurrentTasksPage(
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size) {
        com.hrp.common.entity.PageResult<ProcessTask> pageResult = processTaskService.getAllCurrentTasksPage(page, size);
        return Result.success(pageResult);
    }

    /**
     * 根据业务主键查询任务列表
     */
    @GetMapping("/business-key/{businessKey}")
    public Result<List<ProcessTask>> getByBusinessKey(@PathVariable("businessKey") String businessKey) {
        List<ProcessTask> list = processTaskService.getByBusinessKey(businessKey);
        return Result.success(list);
    }

    /**
     * 根据任务状态查询任务列表
     */
    @GetMapping("/status/{status}")
    public Result<List<ProcessTask>> getByStatus(@PathVariable("status") String status) {
        List<ProcessTask> list = processTaskService.getByStatus(status);
        return Result.success(list);
    }

    /**
     * 根据任务状态分页查询任务列表
     */
    @GetMapping("/status/{status}/page")
    public Result<com.hrp.common.entity.PageResult<ProcessTask>> getByStatusPage(
            @PathVariable("status") String status,
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size) {
        com.hrp.common.entity.PageResult<ProcessTask> pageResult = processTaskService.getByStatusPage(status, page, size);
        return Result.success(pageResult);
    }

    /**
     * 根据业务主键分页查询任务列表
     */
    @GetMapping("/business-key/{businessKey}/page")
    public Result<com.hrp.common.entity.PageResult<ProcessTask>> getByBusinessKeyPage(
            @PathVariable("businessKey") String businessKey,
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size) {
        com.hrp.common.entity.PageResult<ProcessTask> pageResult = processTaskService.getByBusinessKeyPage(businessKey, page, size);
        return Result.success(pageResult);
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
     * 获取下一个审批节点信息（根据流程实例ID）
     */
    @GetMapping("/next-node/{instanceId}")
    public Result<com.hrp.common.entity.NextNodeInfo> getNextNodeInfo(@PathVariable("instanceId") Long instanceId) {
        com.hrp.common.entity.NextNodeInfo nextNodeInfo = processTaskService.getNextNodeInfo(instanceId);
        return Result.success(nextNodeInfo);
    }

    /**
     * 获取下一个审批节点信息（根据业务主键）
     */
    @GetMapping("/next-node/business-key/{businessKey}")
    public Result<com.hrp.common.entity.NextNodeInfo> getNextNodeInfoByBusinessKey(@PathVariable("businessKey") String businessKey) {
        com.hrp.common.entity.NextNodeInfo nextNodeInfo = processTaskService.getNextNodeInfoByBusinessKey(businessKey);
        return Result.success(nextNodeInfo);
    }
    
    /**
     * 根据流程定义生成流程任务记录
     */
    @PostMapping("/generate/{processDefinitionId}/{taskKey}")
    public Result<List<ProcessTask>> generateTasks(@PathVariable("processDefinitionId") Long processDefinitionId, 
                                                     @PathVariable("taskKey") String taskKey,
                                                     @RequestParam(value = "startFromNodeName", required = false) String startFromNodeName) {
        List<ProcessTask> tasks = processTaskService.generateTasksFromDefinition(processDefinitionId, taskKey, startFromNodeName);
        return Result.success(tasks);
    }
    
    /**
     * 根据taskKey删除流程任务记录
     */
    @DeleteMapping("/task-key/{taskKey}")
    public Result<String> deleteByTaskKey(@PathVariable("taskKey") String taskKey) {
        int count = processTaskService.deleteByTaskKey(taskKey);
        return Result.success("删除成功，共删除 " + count + " 条记录");
    }
    
    /**
     * 根据taskId删除单个流程任务记录
     */
    @DeleteMapping("/{taskId}")
    public Result<Boolean> deleteTaskById(@PathVariable("taskId") String taskId) {
        boolean success = processTaskService.deleteTaskById(taskId);
        if (success) {
            return Result.success(true);
        }
        return Result.error("删除失败");
    }
    
    /**
     * 根据taskKey查询任务列表
     */
    @GetMapping("/task-key/{taskKey}")
    public Result<List<ProcessTask>> getByTaskKey(@PathVariable("taskKey") String taskKey) {
        List<ProcessTask> tasks = processTaskService.getByTaskKey(taskKey);
        return Result.success(tasks);
    }
    
    /**
     * 完成任务（同意审批）
     */
    @PostMapping("/complete")
    public Result<Boolean> completeTask(@RequestBody Map<String, Object> request) {
        String taskKey = (String) request.get("taskKey");
        String taskId = (String) request.get("taskId");
        String comment = (String) request.get("comment");
        String approverSignature = (String) request.get("approverSignature");
        
        if (taskKey == null || taskKey.trim().isEmpty()) {
            return Result.error("taskKey不能为空");
        }
        
        boolean success = processTaskService.completeTask(taskKey, taskId, comment, approverSignature);
        if (success) {
            return Result.success(true);
        } else {
            return Result.error("完成任务失败");
        }
    }
    
    /**
     * 加签（在当前任务和下一个任务之间插入新任务）
     */
    @PostMapping("/add-sign")
    public Result<String> addSign(@RequestBody Map<String, Object> request) {
        String taskId = (String) request.get("taskId");
        String newAssigneeUserId = (String) request.get("newAssigneeUserId");
        String newAssigneeUserName = (String) request.get("newAssigneeUserName");
        String newAssigneeEmpCode = (String) request.get("newAssigneeEmpCode");
        String taskName = (String) request.get("taskName");
        
        if (taskId == null || taskId.trim().isEmpty()) {
            return Result.error("taskId不能为空");
        }
        
        if (newAssigneeUserId == null || newAssigneeUserId.trim().isEmpty()) {
            return Result.error("新审批人ID不能为空");
        }
        
        String newTaskId = processTaskService.addSign(taskId, newAssigneeUserId, newAssigneeUserName, newAssigneeEmpCode, taskName);
        if (newTaskId != null) {
            return Result.success("加签成功", newTaskId);
        } else {
            return Result.error("加签失败");
        }
    }
    
    /**
     * 退回任务
     */
    @PostMapping("/return-task")
    public Result<Boolean> returnTask(@RequestBody Map<String, Object> request) {
        String taskKey = (String) request.get("taskKey");
        String taskId = (String) request.get("taskId");
        String returnType = (String) request.get("returnType");
        String applicantUserId = (String) request.get("applicantUserId");
        String applicantUserName = (String) request.get("applicantUserName");
        String applicantEmpCode = (String) request.get("applicantEmpCode");
        String comment = (String) request.get("comment");
        
        if (taskKey == null || taskKey.trim().isEmpty()) {
            return Result.error("taskKey不能为空");
        }
        
        if (returnType == null || (!"RETURN_TO_START".equals(returnType) && !"RETURN_TO_CURRENT".equals(returnType))) {
            return Result.error("退回类型无效，必须是RETURN_TO_START或RETURN_TO_CURRENT");
        }
        
        boolean success = processTaskService.returnTask(taskKey, taskId, returnType, applicantUserId, applicantUserName, applicantEmpCode, comment);
        if (success) {
            return Result.success("退回成功", true);
        } else {
            return Result.error("退回失败");
        }
    }

    /**
     * 查询所有流程实例（从流程任务表获取）
     */
    @GetMapping("/instances")
    public Result<List<ProcessInstance>> getAllProcessInstances() {
        List<ProcessInstance> list = processTaskService.getAllProcessInstances();
        return Result.success(list);
    }
    
    /**
     * 分页查询所有流程实例（从流程任务表获取）
     */
    @GetMapping("/instances/page")
    public Result<com.hrp.common.entity.PageResult<ProcessInstance>> getAllProcessInstancesPage(
            @RequestParam(value = "page", defaultValue = "1") Long page,
            @RequestParam(value = "size", defaultValue = "10") Long size) {
        com.hrp.common.entity.PageResult<ProcessInstance> pageResult = processTaskService.getAllProcessInstancesPage(page, size);
        return Result.success(pageResult);
    }
    
    /**
     * 终止流程实例（根据businessKey）
     */
    @PostMapping("/instance/terminate")
    public Result<Void> terminateProcessInstance(@RequestParam(value = "businessKey") String businessKey) {
        boolean success = processTaskService.terminateProcessInstance(businessKey);
        if (success) {
            return Result.success();
        } else {
            return Result.error("终止流程失败");
        }
    }
    
    /**
     * 恢复退回的任务为待审批状态（用于退回后重新提交）
     */
    @PostMapping("/restore-returned-tasks")
    public Result<Boolean> restoreReturnedTasks(@RequestParam(value = "taskKey") String taskKey) {
        if (taskKey == null || taskKey.trim().isEmpty()) {
            return Result.error("taskKey不能为空");
        }
        boolean success = processTaskService.restoreReturnedTasks(taskKey);
        if (success) {
            return Result.success("恢复成功", true);
        } else {
            return Result.error("恢复失败");
        }
    }
    
    /**
     * 根据业务主键和业务类型查询变量列表（从业务表提取）
     */
    @GetMapping("/instance/variables")
    public Result<List<ProcessVariable>> getVariables(@RequestParam(value = "businessKey") String businessKey,
                                                      @RequestParam(value = "businessType") String businessType) {
        List<ProcessVariable> variables = processTaskService.getVariablesByBusinessKey(businessKey, businessType);
        return Result.success(variables);
    }
    
    /**
     * 批量更新流程变量（直接更新业务表字段）
     */
    @PutMapping("/instance/variables")
    public Result<Void> updateVariables(@RequestParam(value = "businessKey") String businessKey,
                                        @RequestParam(value = "businessType") String businessType,
                                        @RequestBody List<ProcessVariable> variables) {
        boolean success = processTaskService.updateVariablesByBusinessKey(businessKey, businessType, variables);
        if (success) {
            return Result.success();
        } else {
            return Result.error("更新流程变量失败");
        }
    }

    /**
     * 转办任务请求对象
     */
    public static class TransferTaskRequest {
        private String taskId;
        private String newAssigneeUserId;
        private String newAssigneeUserName;
        private String newAssigneeEmpCode;

        public String getTaskId() {
            return taskId;
        }

        public void setTaskId(String taskId) {
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



