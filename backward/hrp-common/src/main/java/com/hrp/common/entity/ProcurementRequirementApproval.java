package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 采购需求审批记录实体
 */
@Data
public class ProcurementRequirementApproval implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long approvalId;
    private Long requirementId;
    private String approverId;
    private String approverCode;
    private String approverName;
    private String approvalType; // 审批类型：APPROVE-同意，REJECT-拒绝
    private String approvalOpinion;
    private LocalDateTime approvalTime;
    private Long taskId;
}












