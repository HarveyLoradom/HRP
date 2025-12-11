package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 资产审批记录实体
 */
@Data
public class AssetApprovalRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long recordId;
    private Long approvalId;
    private String approverId;
    private String approverCode;
    private String approverName;
    private String approvalType; // 审批类型：APPROVE-同意，REJECT-拒绝
    private String approvalOpinion;
    private LocalDateTime approvalTime;
    private Long taskId;
}



