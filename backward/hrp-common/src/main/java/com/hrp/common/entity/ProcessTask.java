package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 流程任务实体
 */
@Data
public class ProcessTask implements Serializable {
    private static final long serialVersionUID = 1L;

    private String taskId; // 任务ID (UUID)
    private Long processInstanceId;
    private String taskKey; // 任务KEY（对于预算申请，就是apply_no）
    private String taskName;
    private String nextTaskId; // 下一节点任务ID (UUID)
    private String assigneeUserId;
    private String assigneeUserName;
    private String assigneeEmpCode;
    private String assigneeType; // 审批人类型：user-指定用户，position-指定岗位，dept-部门负责人，manage_dept-归口审批人
    private String positionCode; // 岗位代码（当 assigneeType = position 时使用）
    private String deptCode; // 部门代码（当 assigneeType = dept 时使用，INITIATOR_DEPT 表示发起人部门）
    private String candidateUsers; // JSON格式
    private String taskStatus; // PENDING-待办理，COMPLETED-已完成，TRANSFERRED-已转办
    private String taskType; // APPROVE-审批，REVIEW-审核
    private String approvalType; // SINGLE-单人审批，COUNTERSIGN-会签（全部同意），OR_SIGN-或签（任意一人同意）
    private String approverList; // 审批人列表（JSON格式，包含所有需要审批的人员）
    private String approvedList; // 已审批人列表（JSON格式，记录已审批的人员和结果）
    private Integer needPrint; // 是否需要打印：0-否，1-是
    private Integer printOrder; // 打印顺序
    private String parentTaskId; // 父任务ID（用于加签场景，UUID）
    private Integer isAddsignTask; // 是否为加签任务：0-否，1-是
    private Integer allowAddsign; // 允许加签：0-否，1-是
    private Integer allowTransfer; // 允许转办：0-否，1-是
    private Integer allowReject; // 允许退回：0-否，1-是
    private Integer allowReduceSign; // 允许减签：0-否，1-是
    private Integer priority;
    private LocalDateTime dueDate;
    private LocalDateTime claimTime;
    private LocalDateTime completeTime;
    private String comment; // 审批意见
    private String approverSignature; // 审批人手写签名（Base64图片数据）
    private String returnType; // 退回类型：RETURN_TO_CURRENT-退回后重新提交到本节点，RETURN_TO_START-退回后重新走流程
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 关联信息
    private String businessKey; // 业务主键（单号或合同号）
    private String businessType; // 业务类型
    private String processDefinitionName; // 流程定义名称
}



