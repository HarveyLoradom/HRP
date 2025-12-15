package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 流程任务实体
 */
@Data
public class ProcessTask implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long taskId;
    private Long processInstanceId;
    private String taskKey;
    private String taskName;
    private String assigneeUserId;
    private String assigneeUserName;
    private String assigneeEmpCode;
    private String candidateUsers; // JSON格式
    private String taskStatus; // PENDING-待办理，COMPLETED-已完成，TRANSFERRED-已转办
    private String taskType; // APPROVE-审批，REVIEW-审核
    private String approvalType; // SINGLE-单人审批，COUNTERSIGN-会签（全部同意），OR_SIGN-或签（任意一人同意）
    private String approverList; // 审批人列表（JSON格式，包含所有需要审批的人员）
    private String approvedList; // 已审批人列表（JSON格式，记录已审批的人员和结果）
    private Integer needPrint; // 是否需要打印：0-否，1-是
    private Long parentTaskId; // 父任务ID（用于加签场景）
    private Integer isAddsignTask; // 是否为加签任务：0-否，1-是
    private Integer priority;
    private LocalDateTime dueDate;
    private LocalDateTime claimTime;
    private LocalDateTime completeTime;
    private String comment; // 审批意见
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 关联信息
    private String businessKey; // 业务主键（单号或合同号）
    private String businessType; // 业务类型
    private String processDefinitionName; // 流程定义名称
}



