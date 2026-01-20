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
    private String createUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}













