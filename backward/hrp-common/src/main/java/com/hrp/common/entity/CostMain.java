package com.hrp.common.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.hrp.common.util.LocalDateTimeDeserializer;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 成本主表实体
 */
@Data
public class CostMain implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long costId;
    private String costNo; // 成本单据编号
    private Long cycleId; // 核算周期ID
    private String cycleCode; // 周期编码
    private String cycleName; // 周期名称
    private Long deptId; // 所属部门ID
    private String deptCode; // 部门编码
    private String deptName; // 部门名称
    private String elementType; // 成本要素
    private String elementTypeName; // 成本要素名称
    private BigDecimal costAmount; // 成本金额
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate occurDate; // 成本实际发生日期
    private String payType; // 付款方式
    private String payTypeName; // 付款方式名称
    private String remark; // 备注说明
    private String createUser; // 录入人
    private String createUserName; // 录入人姓名
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createTime; // 数据录入时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime updateTime; // 数据更新时间
}

