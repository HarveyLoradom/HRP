package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 流程实例实体
 */
@Data
public class ProcessInstance implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long instanceId;
    private Long processDefinitionId;
    private String businessKey; // 单号或合同号
    private String businessType; // PAYOUT-报账，CONTRACT-合同
    private Long businessId; // payout_id或pact_id
    private String processStatus; // RUNNING-运行中，COMPLETED-已完成，TERMINATED-已终止，SUSPENDED-已挂起
    private String startUserId;
    private String startUserName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long duration; // 持续时间（毫秒）
    private String remark;
}



