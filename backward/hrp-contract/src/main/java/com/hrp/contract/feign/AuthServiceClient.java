package com.hrp.contract.feign;

import com.hrp.common.entity.Result;
import com.hrp.common.entity.TemplateConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Auth服务Feign客户端
 * 用于调用auth服务的接口
 */
@FeignClient(name = "hrp-auth", path = "/auth")
public interface AuthServiceClient {
    
    /**
     * 根据流程定义生成流程任务记录
     * 
     * @param processDefinitionId 流程定义ID
     * @param taskKey 任务KEY（对于合同，就是contract_no）
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
     * @param taskKey 任务KEY（对于合同，就是contract_no）
     * @return 删除结果
     */
    @org.springframework.web.bind.annotation.DeleteMapping("/process-task/task-key/{taskKey}")
    Result<String> deleteTasksByTaskKey(@PathVariable("taskKey") String taskKey);
    
    /**
     * 根据taskKey查询任务列表
     * 
     * @param taskKey 任务KEY（对于合同，就是contract_no）
     * @return 任务列表
     */
    @GetMapping("/process-task/task-key/{taskKey}")
    Result<java.util.List<com.hrp.common.entity.ProcessTask>> getTasksByTaskKey(@PathVariable("taskKey") String taskKey);
    
    /**
     * 恢复退回的任务为待审批状态（用于退回后重新提交）
     * 
     * @param taskKey 任务KEY（对于合同，就是contract_no）
     * @return 恢复结果
     */
    @PostMapping("/process-task/restore-returned-tasks")
    Result<Boolean> restoreReturnedTasks(@RequestParam("taskKey") String taskKey);
    
    /**
     * 根据ID获取模板配置
     * 
     * @param id 模板配置ID
     * @return 模板配置
     */
    @GetMapping("/template-config/{id}")
    Result<TemplateConfig> getTemplateConfigById(@PathVariable("id") Long id);

    /**
     * 根据审批人ID查询任务列表（待审批）
     */
    @GetMapping("/process-task/assignee/{userId}")
    Result<java.util.List<com.hrp.common.entity.ProcessTask>> getTasksByAssignee(@PathVariable("userId") String userId);

    /**
     * 根据审批人ID和状态查询任务列表
     */
    @GetMapping("/process-task/assignee/{userId}/status/{status}")
    Result<java.util.List<com.hrp.common.entity.ProcessTask>> getTasksByAssigneeAndStatus(
            @PathVariable("userId") String userId,
            @PathVariable("status") String status);

    /**
     * 退回任务
     */
    @PostMapping("/process-task/return-task")
    Result<Boolean> returnTask(java.util.Map<String, Object> request);
    
    /**
     * 完成任务（同意审批）
     */
    @PostMapping("/process-task/complete")
    Result<Boolean> completeTask(java.util.Map<String, Object> request);
    
    /**
     * 根据businessId删除附件
     * 
     * @param businessId 业务ID（对于合同，可以是mainAttachId或contractNo）
     * @return 删除结果
     */
    @org.springframework.web.bind.annotation.DeleteMapping("/attachment/business-id")
    Result<Void> deleteAttachmentsByBusinessId(@RequestParam("businessId") String businessId);

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
     * 根据账号查询用户信息
     */
    @GetMapping("/user/account/{account}")
    Result<com.hrp.common.entity.User> getUserByAccount(@PathVariable("account") String account);

    /**
     * 根据用户ID查询用户信息
     */
    @GetMapping("/user/{userId}")
    Result<com.hrp.common.entity.User> getUserById(@PathVariable("userId") String userId);
}


