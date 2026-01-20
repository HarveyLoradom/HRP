package com.hrp.common.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.hrp.common.util.LocalDateTimeDeserializer;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预算申请实体
 */
@Data
public class BudgetApply implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long applyId;
    private String applyNo; // 申请单号
    private String budgetYear; // 预算年度
    private Long itemId; // 预算项目ID
    private String itemCode;
    private String itemName;
    private Long subjectId; // 预算主体ID
    private String subjectCode;
    private String subjectName;
    private BigDecimal applyAmount; // 申请金额
    private String applyReason; // 申请事由
    private String status; // DRAFT-草稿，PENDING-待审批，APPROVED-已审批，REJECTED-已拒绝
    private Long processDefinitionId; // 流程定义ID
    private Long processInstanceId; // 流程实例ID
    private Long applicantId; // 申请人ID
    private String applicantCode; // 申请人工号
    private String applicantName; // 申请人姓名
    private Long deptId; // 申请人部门ID
    private String deptCode; // 申请人部门编码
    private String deptName; // 申请人部门名称
    private String applicantPhone; // 申请人手机号
    private Long templateConfigId; // 模板配置ID
    private String mainAttachId; // 主附件ID（时间戳，用于附件关联和文件夹命名）
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime applyDate; // 申请时间
    private String createUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 临时字段：当前审批人（用于审批列表显示，不存数据库）
    @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
    private String currentApprover; // 当前审批人姓名
}

