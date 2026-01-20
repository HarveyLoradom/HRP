package com.hrp.common.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.hrp.common.util.LocalDateTimeDeserializer;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 薪酬核算表实体
 */
@Data
public class HrSalCalculate implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long calcId;
    private Long empId;
    private String empCode; // 员工编码（关联查询）
    private String empName; // 员工姓名（关联查询）
    private String calcMonth; // 核算月份（yyyy-MM）
    private BigDecimal basicSalary; // 基本工资
    private BigDecimal postAllowance; // 岗位津贴
    private BigDecimal overtimeSalary; // 加班工资
    private BigDecimal leaveDeduct; // 请假扣薪
    private BigDecimal absentDeduct; // 旷工扣罚
    private BigDecimal totalIncome; // 应发工资
    private BigDecimal socialSecurity; // 个人社保代扣
    private BigDecimal providentFund; // 个人公积金代扣
    private BigDecimal personalTax; // 个人所得税
    private BigDecimal netSalary; // 实发工资
    private String calcStatus; // 核算状态：UNCALC-未核算，CALC-已核算，PAID-已发放
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime updateTime;
}

