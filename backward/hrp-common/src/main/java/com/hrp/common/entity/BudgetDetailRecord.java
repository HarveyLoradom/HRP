package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预算明细记录实体（对应 budget_detail 表）
 */
@Data
public class BudgetDetailRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long detailId;
    private String budgetYear;
    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private Long itemId;
    private String itemCode;
    private String itemName;
    private BigDecimal amount;
    private String detailType; // 明细类型：APPLY-申请单，PAYOUT-报账单
    private Long businessId;
    private String businessNo; // 业务单号（申请单号或报账单号等）
    private Long deptId;
    private String deptCode;
    private String deptName;
    private Long empId;
    private String empCode;
    private String empName;
    private String remark;
    private String createUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    // 剩余可执行金额（不映射到数据库，仅用于返回）
    private BigDecimal remainingAmount;
}

