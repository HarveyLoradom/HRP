package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 预算参数设置实体
 */
@Data
public class BudgetParam implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long paramId;
    private String paramCode;
    private String paramName;
    private String paramValue;
    private String paramType; // PERIOD-预算周期，WARNING-预警阈值，INTERCEPT-拦截规则
    private String paramDesc;
    private Long isStop;
    private String createUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}








