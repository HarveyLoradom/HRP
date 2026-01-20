package com.hrp.budg.feign;

import com.hrp.common.entity.Result;
import com.hrp.common.entity.ProcessNodeInfo;
import com.hrp.common.entity.ProcessInstance;
import com.hrp.common.entity.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * Auth服务Feign客户端
 * 用于调用auth服务的接口
 */
@FeignClient(name = "hrp-auth", path = "/auth")
public interface AuthServiceClient {
    
    /**
     * 获取流程定义节点信息（根据业务数据动态获取审批人）
     * 
     * @param definitionId 流程定义ID
     * @param applyNo 申请单号
     * @return 流程节点信息列表
     */
    @GetMapping("/process-definition/{definitionId}/nodes/business")
    Result<List<ProcessNodeInfo>> getProcessNodesWithBusiness(
            @PathVariable("definitionId") Long definitionId,
            @RequestParam("applyNo") String applyNo);
    
    /**
     * 根据账号查询用户信息
     * 
     * @param account 用户账号
     * @return 用户信息
     */
    @GetMapping("/user/account/{account}")
    Result<User> getUserByAccount(@PathVariable("account") String account);
    
    /**
     * 根据用户ID查询用户信息
     * 
     * @param userId 用户ID
     * @return 用户信息
     */
    @GetMapping("/user/{userId}")
    Result<User> getUserById(@PathVariable("userId") String userId);
    
    /**
     * 启动流程实例并自动完成"提交"节点
     * 
     * @param request 启动流程请求（使用Map传递参数）
     * @return 流程实例
     */
    @PostMapping("/process-instance/start-and-complete-submit")
    Result<ProcessInstance> startProcessAndAutoCompleteSubmit(@RequestBody java.util.Map<String, Object> request);
    
    /**
     * 根据流程定义生成流程任务记录
     * 
     * @param processDefinitionId 流程定义ID
     * @param taskKey 任务KEY（对于预算申请，就是apply_no）
     * @param startFromNodeName 从指定节点名称开始生成任务（可选）
     * @return 生成的任务列表
     */
    @PostMapping("/process-task/generate/{processDefinitionId}/{taskKey}")
    Result<java.util.List<com.hrp.common.entity.ProcessTask>> generateTasks(
            @PathVariable("processDefinitionId") Long processDefinitionId,
            @PathVariable("taskKey") String taskKey,
            @RequestParam(value = "startFromNodeName", required = false) String startFromNodeName);
    
    /**
     * 根据taskKey删除流程任务记录
     * 
     * @param taskKey 任务KEY（对于预算申请，就是apply_no）
     * @return 删除结果
     */
    @org.springframework.web.bind.annotation.DeleteMapping("/process-task/task-key/{taskKey}")
    Result<String> deleteTasksByTaskKey(@PathVariable("taskKey") String taskKey);
    
    /**
     * 根据taskId删除单个流程任务记录
     * 
     * @param taskId 任务ID
     * @return 是否成功
     */
    @org.springframework.web.bind.annotation.DeleteMapping("/process-task/{taskId}")
    Result<Boolean> deleteTaskById(@PathVariable("taskId") String taskId);
    
    /**
     * 根据taskKey查询任务列表
     * 
     * @param taskKey 任务KEY（对于预算申请，就是apply_no）
     * @return 任务列表
     */
    @GetMapping("/process-task/task-key/{taskKey}")
    Result<java.util.List<com.hrp.common.entity.ProcessTask>> getTasksByTaskKey(@PathVariable("taskKey") String taskKey);
    
    /**
     * 完成任务（同意审批）
     * 
     * @param request 完成任务请求（包含taskKey, taskId, comment）
     * @return 是否成功
     */
    @PostMapping("/process-task/complete")
    Result<Boolean> completeTask(@RequestBody java.util.Map<String, Object> request);
    
    /**
     * 加签（在当前任务和下一个任务之间插入新任务）
     * 
     * @param request 加签请求（包含taskId, newAssigneeUserId, newAssigneeUserName, newAssigneeEmpCode, taskName）
     * @return 新创建的任务ID
     */
    @PostMapping("/process-task/add-sign")
    Result<String> addSign(@RequestBody java.util.Map<String, Object> request);
    
    /**
     * 根据业务类型和业务ID删除附件（包括文件系统中的文件）
     * 
     * @param businessType 业务类型
     * @param businessId 业务ID
     * @return 是否成功
     */
    @org.springframework.web.bind.annotation.DeleteMapping("/attachment/business")
    Result<Void> deleteAttachmentsByBusiness(@RequestParam("businessType") String businessType, 
                                             @RequestParam("businessId") String businessId);
    
    /**
     * 根据办理人ID查询任务列表（待审批任务）
     * 
     * @param userId 用户ID
     * @return 任务列表
     */
    @GetMapping("/process-task/assignee/{userId}")
    Result<java.util.List<com.hrp.common.entity.ProcessTask>> getTasksByAssignee(@PathVariable("userId") String userId);
    
    /**
     * 根据办理人ID和任务状态查询任务列表（用于查询已审批等历史记录）
     * 
     * @param userId 用户ID
     * @param taskStatus 任务状态
     * @return 任务列表
     */
    @GetMapping("/process-task/assignee/{userId}/status/{taskStatus}")
    Result<java.util.List<com.hrp.common.entity.ProcessTask>> getTasksByAssigneeAndStatus(@PathVariable("userId") String userId, @PathVariable("taskStatus") String taskStatus);
    
    /**
     * 退回任务
     * 
     * @param request 退回请求（包含taskKey, taskId, returnType, applicantUserId, applicantUserName, applicantEmpCode, comment）
     * @return 是否成功
     */
    @PostMapping("/process-task/return-task")
    Result<Boolean> returnTask(@RequestBody java.util.Map<String, Object> request);
    
    /**
     * 恢复退回的任务为待审批状态（用于退回后重新提交）
     * 
     * @param taskKey 任务KEY
     * @return 是否成功
     */
    @PostMapping("/process-task/restore-returned-tasks")
    Result<Boolean> restoreReturnedTasks(@RequestParam("taskKey") String taskKey);
}

