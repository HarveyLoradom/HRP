package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 资产审批实体
 */
@Data
public class AssetApproval implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long approvalId;
    private String approvalNo;
    private String approvalType; // 审批类型：TRANSFER-资产调拨，DISPOSAL-资产处置，INVENTORY_DIFF-盘点差异，CHANGE-资产变动
    private String approvalTitle;
    private Long deptId;
    private String deptCode;
    private String deptName;
    private Long applicantId;
    private String applicantCode;
    private String applicantName;
    private String applyReason;
    private String status; // 状态：DRAFT-草稿，PENDING-待审批，APPROVED-已审批，REJECTED-已拒绝
    private Long processDefinitionId;
    private Long processInstanceId;
    private String createUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}












