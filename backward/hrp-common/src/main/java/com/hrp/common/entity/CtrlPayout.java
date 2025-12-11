package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 报账实体
 */
@Data
public class CtrlPayout implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long payoutId;
    private String payoutBillcode;
    private String billType; // 单据类型：APPLY-申请单，PAYOUT-报账单
    private Long empId;
    private String empCode;
    private String empName;
    private Long deptId;
    private String payoutTypeId;
    private BigDecimal applyAmount;
    private String applyReason;
    private LocalDateTime applyDate;
    private String status;
    private Long processDefinitionId;
    private Long processInstanceId;
    private Long budgetId; // 预算ID
    private Long budgetItemId; // 预算项目ID
    private String remark;
    private String createUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

