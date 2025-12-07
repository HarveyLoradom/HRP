package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 设备收入实体
 */
@Data
public class IncomeEquipment implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long incomeId;
    private String incomeNo;
    private LocalDate incomeDate;
    private Long equipmentId;
    private String equipmentCode;
    private String incomeType;
    private BigDecimal incomeAmount;
    private LocalDateTime createTime;
}

