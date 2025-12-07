package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * HIS收入实体
 */
@Data
public class IncomeHis implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long incomeId;
    private String incomeNo;
    private LocalDate incomeDate;
    private Long deptId;
    private Long clinicId;
    private String incomeType;
    private BigDecimal incomeAmount;
    private String hisSystemNo;
    private LocalDateTime createTime;
}

