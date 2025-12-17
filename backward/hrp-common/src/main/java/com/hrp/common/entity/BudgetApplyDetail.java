package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预算申请明细实体（用于查看申请金额明细）
 */
@Data
public class BudgetApplyDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    private String applyNo; // 申请单号
    private Long deptId;
    private String deptName; // 科室
    private Long applicantId;
    private String applicantName; // 申请人
    private BigDecimal applyAmount; // 申请金额
    private String status; // 状态
    private LocalDateTime createTime; // 申请时间
}

