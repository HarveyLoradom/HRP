package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 流程变量实体
 */
@Data
public class ProcessVariable implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long variableId;
    private Long processInstanceId;
    private String variableKey;
    private String variableValue;
    private String variableType; // STRING-字符串，INTEGER-整数，DOUBLE-浮点数，BOOLEAN-布尔值，DATE-日期
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}



