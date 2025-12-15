package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 加签记录实体
 */
@Data
public class AddSignRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long addsignId;
    private Long processInstanceId;
    private Long parentTaskId; // 被加签的任务ID
    private Long addsignTaskId; // 加签后创建的任务ID
    private String addsignUserId; // 执行加签操作的用户ID
    private String addsignUserName;
    private String addsignEmpCode;
    private String targetUserId; // 被加签人ID（需要审批的用户）
    private String targetUserName;
    private String targetEmpCode;
    private String addsignReason; // 加签原因
    private String addsignStatus; // PENDING-待审批，COMPLETED-已完成，CANCELLED-已取消
    private LocalDateTime completeTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

