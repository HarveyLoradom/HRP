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



