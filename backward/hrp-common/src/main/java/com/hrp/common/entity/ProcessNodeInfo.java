package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;

/**
 * 流程节点信息
 */
@Data
public class ProcessNodeInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    // 基本信息
    private String id; // 节点ID
    private String name; // 节点名称
    private String type; // 节点类型（userTask, serviceTask, gateway, sequenceFlow等）
    private String description; // 节点描述
    
    // 审批人配置
    private String approvalType; // 审批类型（SINGLE-单人审批, MULTI-会签全部通过, OR-或签任一通过）
    private String assigneeType; // 审批人类型（user-指定用户, position-指定岗位, dept-部门负责人, manage_dept-归口审批人, initiator-发起人, previous-上一节点审批人, responsible-负责人）
    private String assigneeTypeText; // 审批人类型文本（指定用户、指定岗位等）
    private String assigneeId; // 审批人ID
    private String assigneeName; // 审批人姓名
    private String assigneeCode; // 审批人代码
    private String positionCode; // 岗位代码
    private String deptCode; // 部门代码
    private String responsibleType; // 负责人类型（DEPT_MANAGER-发起人部门负责人, NURSE_MANAGER-发起人部门护士长, VICE_PRESIDENT-发起人部门分管院长）
    
    // 会签配置
    private String multiInstanceType; // 会签策略（ALL-全部通过, ANY-任一通过, PERCENT-按比例通过）
    private Integer multiInstanceCount; // 会签人数
    private Integer completionCondition; // 通过比例（当multiInstanceType为PERCENT时）
    
    // 高级配置
    private Integer allowAddsign; // 允许加签（0-否，1-是）
    private Integer allowReduceSign; // 允许减签（0-否，1-是）
    private Integer allowTransfer; // 允许转办（0-否，1-是）
    private Integer allowReject; // 允许退回（0-否，1-是）
    private String rejectStrategy; // 退回策略（PREVIOUS-退回上一节点, START-退回发起人, SPECIFY-退回指定节点）
    
    // 打印配置
    private Integer needPrint; // 需要打印（0-否，1-是）
    private Integer printOrder; // 打印顺序
    
    // 超时配置
    private Integer enableTimeout; // 超时设置（0-否，1-是）
    private Integer timeoutHours; // 超时时间（小时）
    private String timeoutAction; // 超时处理（AUTO_PASS-自动通过, AUTO_REJECT-自动拒绝, NOTIFY-通知提醒）
    
    // 网关配置
    private String gatewayType; // 网关类型（EXCLUSIVE-排他网关, PARALLEL-并行网关, INCLUSIVE-包容网关）
    
    // 连线条件配置
    private String conditionType; // 条件类型（none-无条件, expression-表达式, script-脚本）
    private String conditionExpression; // 条件表达式
}

