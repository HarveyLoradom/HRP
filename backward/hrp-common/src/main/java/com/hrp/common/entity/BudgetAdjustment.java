package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预算调整实体
 */
@Data
public class BudgetAdjustment implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long adjustmentId;
    private String adjustmentNo;
    private Long budgetId;
    private String budgetNo;
    private String budgetName; // 关联查询
    private Long subjectId;
    private String subjectName; // 关联查询
    private Long itemId;
    private String itemName; // 关联查询
    private BigDecimal originalAmount;
    private BigDecimal adjustmentAmount; // 调整金额（正数为增加，负数为减少）
    private BigDecimal adjustedAmount; // 调整后金额
    private String adjustmentReason;
    private String status; // PENDING-待审批，APPROVED-已审批，REJECTED-已拒绝
    private Long processDefinitionId;
    private Long processInstanceId;
    private Long applicantId;
    private String applicantName;
    private Long approverId;
    private String approverName;
    private LocalDateTime approveTime;
    private String approveOpinion;
    private String createUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

