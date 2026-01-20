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
    private String adjustmentType; // 调整类型：QUOTA_INCREASE-额度调增，QUOTA_DECREASE-额度调减，APPLY_OFFSET-申请冲销，PAYOUT_OFFSET-报账冲销
    private Long relatedBillId; // 关联单据ID（申请单或报账单ID）
    private String relatedBillNo; // 关联单据号（申请单号或报账单号）
    private String relatedBillType; // 关联单据类型：APPLY-申请单，PAYOUT-报账单
    private Long budgetId;
    private String budgetNo;
    private String budgetName; // 关联查询
    private Long subjectId;
    private String subjectCode;
    private String subjectName; // 关联查询
    private Long itemId;
    private String itemCode;
    private String itemName; // 关联查询
    private BigDecimal originalAmount;
    private BigDecimal adjustmentAmount; // 调整金额（正数为增加，负数为减少）
    private BigDecimal adjustedAmount; // 调整后金额
    private String adjustmentReason; // 调整原因（必填）
    private Long applicantId;
    private String applicantName;
    private String createUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

