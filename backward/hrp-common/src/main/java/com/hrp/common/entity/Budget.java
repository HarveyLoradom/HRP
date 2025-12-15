package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预算实体
 */
@Data
public class Budget implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long budgetId;
    private String budgetNo;
    private String budgetName;
    private String budgetYear;
    private String budgetPeriod; // YEAR-年度，QUARTER-季度，MONTH-月度
    private Long subjectId;
    private String subjectCode;
    private String subjectName; // 关联查询
    private Long itemId;
    private String itemCode;
    private String itemName; // 关联查询
    private BigDecimal budgetAmount;
    private BigDecimal occupiedAmount; // 占用金额（已申请但未执行）
    private BigDecimal executedAmount; // 已执行金额
    private BigDecimal remainingAmount; // 剩余金额
    private BigDecimal executionRate; // 执行率（%）
    private String status; // DRAFT-草稿，APPROVED-已审批，EXECUTING-执行中
    private Long processDefinitionId;
    private Long processInstanceId;
    private String createUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}








