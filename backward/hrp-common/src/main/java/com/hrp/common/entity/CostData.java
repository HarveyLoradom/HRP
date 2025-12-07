package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 成本数据实体
 */
@Data
public class CostData implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long costId;
    private String costNo;
    private LocalDate costDate;
    private Long deptId;
    private String costType;
    private String costItem;
    private BigDecimal costAmount;
    private LocalDateTime createTime;
}

