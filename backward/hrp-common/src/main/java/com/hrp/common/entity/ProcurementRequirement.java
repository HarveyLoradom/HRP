package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 采购需求实体
 */
@Data
public class ProcurementRequirement implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long requirementId;
    private String requirementNo;
    private String requirementName;
    private String requirementType;
    private Long deptId;
    private Long applicantId;
    private BigDecimal estimatedAmount;
    private String requirementDesc;
    private String status;
    private String createUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

