package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预算执行单实体
 */
@Data
public class BudgetExecution implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long executionId;
    private String executionNo;
    private Long budgetId;
    private String budgetNo;
    private Long subjectId;
    private String subjectName; // 关联查询
    private Long itemId;
    private String itemName; // 关联查询
    private String executionType; // AUTO-自动同步，MANUAL-手工录入
    private BigDecimal executionAmount;
    private LocalDateTime executionDate;
    private String businessType; // PAYOUT-报账，CONTRACT-合同，PROCUREMENT-采购
    private Long businessId;
    private String businessNo;
    private String executionReason;
    private String status; // PENDING-待审核，APPROVED-已审核，REJECTED-已拒绝，CANCELLED-已作废
    private String auditUser;
    private LocalDateTime auditTime;
    private String auditOpinion;
    private String createUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

