package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 成本报表实体
 */
@Data
public class CostReport implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long reportId;
    private String reportNo;
    private String reportName;
    private String reportType;
    private String reportPeriod;
    private Long deptId;
    private BigDecimal totalCost;
    private BigDecimal directCost;
    private BigDecimal indirectCost;
    private String status;
    private String createUser;
    private LocalDateTime createTime;
}

