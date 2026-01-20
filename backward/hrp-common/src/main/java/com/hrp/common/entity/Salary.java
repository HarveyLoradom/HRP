package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 薪酬实体
 */
@Data
public class Salary implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long salaryId;
    private Long empId;
    private String empCode;
    private String empName;
    private String salaryMonth;
    private BigDecimal baseSalary;
    private BigDecimal performanceSalary;
    private BigDecimal allowance;
    private BigDecimal bonus;
    private BigDecimal deduction;
    private BigDecimal totalSalary;
    private BigDecimal tax;
    private BigDecimal actualSalary;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

