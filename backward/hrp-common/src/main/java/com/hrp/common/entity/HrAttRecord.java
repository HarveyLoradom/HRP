package com.hrp.common.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.hrp.common.util.LocalDateTimeDeserializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工考勤记录表（原始打卡明细）实体
 */
@Data
public class HrAttRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long recordId;
    private Long empId;
    private String empCode; // 员工编码（关联查询）
    private String empName; // 员工姓名（关联查询）
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate attDate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime attStartTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime attEndTime;
    private String attStatus;
    private Integer isSupplement;
    private String attType; // 考勤类型（对应hr_apply的hr_apply_type）
    private String attSubType; // 考勤子类型（对应hr_apply的hr_apply_sub_type）
    private Long applyId; // 申请ID（关联hr_apply表的apply_id）
    private String day; // 天数
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime updateTime;
}

