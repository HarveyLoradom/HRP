package com.hrp.common.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.hrp.common.util.LocalDateTimeDeserializer;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 薪酬配置表（按员工配置）实体
 */
@Data
public class HrSalConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer configId;
    private Long empId;
    private String empCode; // 员工编码（关联查询）
    private String empName; // 员工姓名（关联查询）
    private BigDecimal basicSalary; // 基本工资
    private BigDecimal postAllowance; // 岗位津贴
    private BigDecimal socialSecurity; // 个人社保代扣比例
    private BigDecimal providentFund; // 个人公积金代扣比例
    private BigDecimal taxThreshold; // 个税起征点
    private String createUser;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime updateTime;
}

