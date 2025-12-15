package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 成本核算实体
 */
@Data
public class CostAccounting implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long accountingId;
    private String accountingNo;
    private String accountingName;
    private String accountingPeriod;
    private String accountingDimension;
    private Long deptId;
    private String costItem;
    private BigDecimal costAmount;
    private BigDecimal directCost;
    private BigDecimal indirectCost;
    private String status;
    private LocalDateTime createTime;
}

