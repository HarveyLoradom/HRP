package com.hrp.auth.service;

import com.hrp.common.entity.ProcessTask;
import com.hrp.common.entity.ProcessInstance;
import com.hrp.common.entity.ProcessVariable;

import java.util.List;

/**
 * 流程任务服务接口
 */
public interface ProcessTaskService {
    /**
     * 根据ID查询流程任务
     */
    ProcessTask getById(String taskId);

    /**
     * 根据流程实例ID查询任务列表
     */
    List<ProcessTask> getByInstanceId(Long processInstanceId);

    /**
     * 根据办理人ID查询任务列表
     */
    List<ProcessTask> getByAssignee(String assigneeUserId);
    
    /**
     * 根据办理人ID分页查询任务列表
     */
    com.hrp.common.entity.PageResult<ProcessTask> getByAssigneePage(String assigneeUserId, Long page, Long size);
    
    /**
     * 根据办理人ID和任务状态查询任务列表（用于查询已审批等历史记录）
     */
    List<ProcessTask> getByAssigneeAndStatus(String assigneeUserId, String taskStatus);
    
    /**
     * 根据办理人ID和任务状态分页查询任务列表（用于查询已审批等历史记录）
     */
    com.hrp.common.entity.PageResult<ProcessTask> getByAssigneeAndStatusPage(String assigneeUserId, String taskStatus, Long page, Long size);
    
    /**
     * 查询所有当前节点的任务（显示所有流程的当前审批节点）
     */
    List<ProcessTask> getAllCurrentTasks();
    
    /**
     * 分页查询所有当前节点的任务（显示所有流程的当前审批节点）
     */
    com.hrp.common.entity.PageResult<ProcessTask> getAllCurrentTasksPage(Long page, Long size);

    /**
     * 根据业务主键查询任务列表
     */
    List<ProcessTask> getByBusinessKey(String businessKey);

    /**
     * 根据任务状态查询任务列表
     */
    List<ProcessTask> getByStatus(String taskStatus);

    /**
     * 根据任务状态分页查询任务列表
     */
    com.hrp.common.entity.PageResult<ProcessTask> getByStatusPage(String taskStatus, Long page, Long size);

    /**
     * 根据业务主键分页查询任务列表
     */
    com.hrp.common.entity.PageResult<ProcessTask> getByBusinessKeyPage(String businessKey, Long page, Long size);

    /**
     * 转办任务（更改办理人）
     */
    boolean transferTask(String taskId, String newAssigneeUserId, String newAssigneeUserName, String newAssigneeEmpCode);

    /**
     * 获取下一个审批节点信息
     * @param processInstanceId 流程实例ID
     * @return 下一个审批节点信息，如果没有则返回null
     */
    com.hrp.common.entity.NextNodeInfo getNextNodeInfo(Long processInstanceId);

    /**
     * 根据业务主键获取下一个审批节点信息
     * @param businessKey 业务主键
     * @return 下一个审批节点信息，如果没有则返回null
     */
    com.hrp.common.entity.NextNodeInfo getNextNodeInfoByBusinessKey(String businessKey);
    
    /**
     * 根据流程定义和业务数据生成流程任务记录
     * @param processDefinitionId 流程定义ID
     * @param taskKey 任务KEY（对于预算申请，就是apply_no）
     * @param startFromNodeName 从指定节点名称开始生成任务（可选，如果为null则从流程开始生成）
     * @return 生成的任务列表
     */
    List<ProcessTask> generateTasksFromDefinition(Long processDefinitionId, String taskKey, String startFromNodeName);
    
    /**
     * 根据taskKey删除所有流程任务记录
     * @param taskKey 任务KEY（对于预算申请，就是apply_no）
     * @return 删除的记录数
     */
    int deleteByTaskKey(String taskKey);
    
    /**
     * 根据taskId删除单个流程任务记录
     * @param taskId 任务ID
     * @return 是否成功
     */
    boolean deleteTaskById(String taskId);
    
    /**
     * 根据taskKey查询任务列表
     * @param taskKey 任务KEY（对于预算申请，就是apply_no）
     * @return 任务列表
     */
    List<ProcessTask> getByTaskKey(String taskKey);
    
    /**
     * 完成任务（同意审批）
     * @param taskKey 任务KEY（对于预算申请，就是apply_no）
     * @param taskId 任务ID（可选，如果不提供则自动查找当前待处理任务）
     * @param comment 审批意见
     * @param approverSignature 审批人手写签名（Base64图片数据，可选）
     * @return 是否成功
     */
    boolean completeTask(String taskKey, String taskId, String comment, String approverSignature);
    
    /**
     * 加签（在当前任务和下一个任务之间插入新任务）
     * @param taskId 当前任务ID
     * @param newAssigneeUserId 新审批人ID
     * @param newAssigneeUserName 新审批人姓名
     * @param newAssigneeEmpCode 新审批人编码
     * @param taskName 新任务名称（可选，默认使用"加签审批"）
     * @return 新创建的任务ID，失败返回null
     */
    String addSign(String taskId, String newAssigneeUserId, String newAssigneeUserName, String newAssigneeEmpCode, String taskName);
    
    /**
     * 退回任务
     * @param taskKey 任务KEY（对于预算申请，就是apply_no）
     * @param taskId 当前任务ID（可选，如果不提供则自动查找当前待处理任务）
     * @param returnType 退回类型：RETURN_TO_START-退回重新走流程，RETURN_TO_CURRENT-退回修改后提交到本节点
     * @param applicantUserId 申请人用户ID（sys_user.id）
     * @param applicantUserName 申请人姓名
     * @param applicantEmpCode 申请人工号
     * @param comment 退回意见
     * @return 是否成功
     */
    boolean returnTask(String taskKey, String taskId, String returnType, String applicantUserId, String applicantUserName, String applicantEmpCode, String comment);
    
    /**
     * 查询所有流程实例（从流程任务表获取）
     */
    List<ProcessInstance> getAllProcessInstances();
    
    /**
     * 分页查询所有流程实例（从流程任务表获取）
     */
    com.hrp.common.entity.PageResult<ProcessInstance> getAllProcessInstancesPage(Long page, Long size);
    
    /**
     * 根据业务主键和业务类型查询变量列表（从业务表提取）
     */
    List<ProcessVariable> getVariablesByBusinessKey(String businessKey, String businessType);
    
    /**
     * 终止流程实例（根据businessKey终止所有相关任务）
     */
    boolean terminateProcessInstance(String businessKey);
    
    /**
     * 根据业务主键和业务类型批量更新流程变量（直接更新业务表字段）
     */
    boolean updateVariablesByBusinessKey(String businessKey, String businessType, List<ProcessVariable> variables);
    
    /**
     * 恢复退回的任务为待审批状态（用于退回后重新提交）
     * @param taskKey 任务KEY（对于预算申请，就是apply_no）
     * @return 是否成功
     */
    boolean restoreReturnedTasks(String taskKey);
}



