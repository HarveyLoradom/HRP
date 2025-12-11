package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 流程定义实体
 */
@Data
public class ProcessDefinition implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long definitionId;
    private String definitionKey;
    private String definitionName;
    private String definitionType; // PAYOUT-报账审批，CONTRACT-合同审批
    private String processXml; // BPMN格式的流程XML
    private String processJson; // 前端绘制用的JSON格式
    private Integer version;
    private Long isActive; // 0-否，1-是
    private String description;
    private String createUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}



