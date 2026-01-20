package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 预算明细实体（用于查询展示）
 */
@Data
public class BudgetDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long itemId;
    private String itemCode;
    private String itemName;
    private String budgetYear;
    private String categoryType; // INCOME-收入预算，EXPENSE-支出预算
    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private BigDecimal budgetAmount; // 预算总额
    private BigDecimal executedAmount; // 执行金额
    private BigDecimal appliedAmount; // 申请金额
    private BigDecimal remainingAmount; // 剩余可执行金额
}

