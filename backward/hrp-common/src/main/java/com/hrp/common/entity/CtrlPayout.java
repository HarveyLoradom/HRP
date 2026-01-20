package com.hrp.common.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
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
    private String empPhone; // 手机号（从sys_emp表关联获取）
    private Long deptId;
    private String deptName; // 科室名称（从sys_dept表关联获取）
    private Long isNurse; // 是否护士：0-否，1-是
    private String payoutTypeId;
    private BigDecimal applyAmount;
    private String sourceApplyNo;
    private String contractNo; // 合同编号
    private String applyReason;
    private LocalDate applyDate;
    private String status;
    private Long templateConfigId; // 模板配置ID
    private String mainAttachId; // 主附件ID（用于附件关联）
    private Long processDefinitionId;
    private Long processInstanceId;
    private Long budgetId; // 预算ID
    private Long budgetItemId; // 预算项目ID
    private String remark;
    private String createUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

