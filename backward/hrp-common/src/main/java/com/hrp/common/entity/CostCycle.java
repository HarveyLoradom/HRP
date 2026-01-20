package com.hrp.common.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.hrp.common.util.LocalDateTimeDeserializer;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 成本核算周期实体
 */
@Data
public class CostCycle implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long cycleId; // 周期ID
    private String cycleCode; // 周期编码
    private String cycleName; // 周期名称
    private String cycleType; // 周期类型
    private String cycleTypeName; // 周期类型名称
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate startDate; // 周期开始日期
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate endDate; // 周期结束日期
    private Integer status; // 状态：1-启用，0-禁用
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createTime; // 创建时间
}

