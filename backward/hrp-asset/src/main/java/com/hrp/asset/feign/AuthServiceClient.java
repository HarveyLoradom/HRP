package com.hrp.asset.feign;

import com.hrp.common.entity.Result;
import com.hrp.common.entity.TemplateConfig;
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
     * 根据账号查询用户信息
     */
    @GetMapping("/user/account/{account}")
    Result<User> getUserByAccount(@PathVariable("account") String account);
    
    /**
     * 根据用户ID查询用户信息
     */
    @GetMapping("/user/{userId}")
    Result<User> getUserById(@PathVariable("userId") String userId);
    
    /**
     * 根据流程定义生成流程任务记录
     */
    @PostMapping("/process-task/generate/{processDefinitionId}/{taskKey}")
    Result<java.util.List<com.hrp.common.entity.ProcessTask>> generateTasks(
            @PathVariable("processDefinitionId") Long processDefinitionId,
            @PathVariable("taskKey") String taskKey,
            @RequestParam(value = "startFromNodeName", required = false) String startFromNodeName);
    
    /**
     * 根据taskKey删除流程任务记录
     */
    @org.springframework.web.bind.annotation.DeleteMapping("/process-task/task-key/{taskKey}")
    Result<String> deleteTasksByTaskKey(@PathVariable("taskKey") String taskKey);
    
    /**
     * 根据taskKey查询任务列表
     */
    @GetMapping("/process-task/task-key/{taskKey}")
    Result<java.util.List<com.hrp.common.entity.ProcessTask>> getTasksByTaskKey(@PathVariable("taskKey") String taskKey);
    
    /**
     * 完成任务（同意审批）
     */
    @PostMapping("/process-task/complete")
    Result<Boolean> completeTask(@RequestBody java.util.Map<String, Object> request);
    
    /**
     * 根据业务类型和业务ID删除附件（包括文件系统中的文件）
     */
    @org.springframework.web.bind.annotation.DeleteMapping("/attachment/business")
    Result<Void> deleteAttachmentsByBusiness(@RequestParam("businessType") String businessType, 
                                             @RequestParam("businessId") String businessId);
    
    /**
     * 根据业务ID删除附件（包括文件系统中的文件），不依赖businessType
     */
    @org.springframework.web.bind.annotation.DeleteMapping("/attachment/business-id")
    Result<Void> deleteAttachmentsByBusinessId(@RequestParam("businessId") String businessId);
    
    /**
     * 退回任务
     */
    @PostMapping("/process-task/return-task")
    Result<Boolean> returnTask(@RequestBody java.util.Map<String, Object> request);
    
    /**
     * 恢复退回的任务为待审批状态（用于退回后重新提交）
     */
    @PostMapping("/process-task/restore-returned-tasks")
    Result<Boolean> restoreReturnedTasks(@RequestParam("taskKey") String taskKey);
    
    /**
     * 终止任务（拒绝审批）
     */
    @PostMapping("/process-task/terminate")
    Result<Boolean> terminateTask(@RequestBody java.util.Map<String, Object> request);
    
    /**
     * 根据模板配置ID获取模板配置
     */
    @GetMapping("/template-config/{configId}")
    Result<TemplateConfig> getTemplateConfigById(@PathVariable("configId") Long configId);
    
    /**
     * 根据办理人ID查询任务列表（待审批任务）
     */
    @GetMapping("/process-task/assignee/{userId}")
    Result<java.util.List<com.hrp.common.entity.ProcessTask>> getTasksByAssignee(@PathVariable("userId") String userId);
    
    /**
     * 根据办理人ID和任务状态查询任务列表（用于查询已审批等历史记录）
     */
    @GetMapping("/process-task/assignee/{userId}/status/{taskStatus}")
    Result<java.util.List<com.hrp.common.entity.ProcessTask>> getTasksByAssigneeAndStatus(@PathVariable("userId") String userId, @PathVariable("taskStatus") String taskStatus);
    
    /**
     * 获取下一节点信息
     */
    @GetMapping("/process-task/next-node-info")
    Result<com.hrp.common.entity.ProcessNodeInfo> getNextNodeInfoByBusinessKey(@RequestParam("businessKey") String businessKey);
}

