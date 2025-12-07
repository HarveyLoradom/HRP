package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 薪酬计算规则实体
 */
@Data
public class SalaryRule implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long ruleId;
    private String ruleCode;
    private String ruleName;
    private String ruleType;
    private String ruleFormula;
    private Long isActive;
    private LocalDateTime createTime;
}

