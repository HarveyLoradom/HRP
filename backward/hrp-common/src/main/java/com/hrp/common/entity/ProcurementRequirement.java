package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 采购需求实体
 */
@Data
public class ProcurementRequirement implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long requirementId;
    private String requirementNo;
    private String requirementName;
    private String requirementType;
    private Long deptId;
    private String deptCode;
    private String deptName;
    private Long applicantId;
    private String applicantCode;
    private String applicantName;
    private BigDecimal estimatedAmount;
    private String requirementDesc;
    private String status; // 状态：DRAFT-草稿，PENDING-待审批，APPROVED-已审批，REJECTED-已拒绝
    private Long processDefinitionId;
    private Long processInstanceId;
    private String createUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

