package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预算执行明细实体（用于查看执行金额明细）
 */
@Data
public class BudgetExecutionDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    private String payoutBillcode; // 报账单号
    private Long deptId;
    private String deptName; // 科室
    private Long empId;
    private String empName; // 申请人
    private BigDecimal executionAmount; // 执行金额
    private LocalDateTime executionDate; // 执行日期
}

