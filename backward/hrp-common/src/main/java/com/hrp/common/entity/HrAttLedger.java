package com.hrp.common.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.hrp.common.util.LocalDateTimeDeserializer;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 员工考勤台账表（月度汇总）实体
 */
@Data
public class HrAttLedger implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long ledgerId;
    private Long empId;
    private String empCode; // 员工编码（关联查询）
    private String empName; // 员工姓名（关联查询）
    private Long deptId;    // 部门ID（关联查询）
    private String deptName; // 部门名称（关联查询）
    private String attMonth; // 考勤月份（yyyy-MM）
    private Integer monthWorkDays; // 本月工作日
    private BigDecimal attDays; // 正常出勤天数（支持0.5）
    private BigDecimal leaveDays; // 请假总天数
    private BigDecimal overtimeDays; // 加班总天数
    private BigDecimal absentDays; // 旷工天数（支持0.5）
    private String ledgerStatus; // 台账状态（sys_code: HR_LEDGER_STATUS）
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime updateTime;
}

