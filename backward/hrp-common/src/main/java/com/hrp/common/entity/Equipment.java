package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 设备实体
 */
@Data
public class Equipment implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long equipmentId;
    private String equipmentCode;
    private String equipmentName;
    private String equipmentType;
    private LocalDateTime purchaseDate;
    private BigDecimal purchaseAmount;
    private Long deptId;
    private Long clinicId;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

