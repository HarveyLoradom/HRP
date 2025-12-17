package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 预算流程配置实体
 */
@Data
public class BudgetProcessConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long configId;
    private String configType; // BUDGET-预算编制，ADJUSTMENT-预算调整
    private Long processDefinitionId;
    private String configJson; // 配置JSON（审批节点、审批人、时效等）
    private Long isActive;
    private String createUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}













