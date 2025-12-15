package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 任务审批记录实体（用于记录会签/或签的每个审批人的审批结果）
 */
@Data
public class TaskApprovalRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long recordId;
    private Long taskId;
    private Long processInstanceId;
    private String approverUserId;
    private String approverUserName;
    private String approverEmpCode;
    private String approvalResult; // APPROVED-同意，REJECTED-拒绝
    private String approvalOpinion;
    private LocalDateTime approvalTime;
    private Integer isAddsign; // 0-否，1-是
    private Long addsignRecordId; // 加签记录ID（如果是加签审批）
}

