package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 审批记录实体
 */
@Data
public class CtrlPayoutApproval implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long approvalId;
    private Long payoutId;
    private Long approverId;
    private String approverCode;
    private String approverName;
    private String approvalType; // 审批类型：APPROVE-通过，REJECT-驳回
    private String approvalOpinion; // 审批意见
    private LocalDateTime approvalTime;
    private Long taskId;
    private String taskName;
    private Integer sortOrder;
}




