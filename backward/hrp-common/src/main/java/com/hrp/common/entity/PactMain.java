package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 合同实体
 */
@Data
public class PactMain implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long pactId;
    private String contractNo;
    private String contractName;
    private String contractType;
    private String partyA;
    private String partyB;
    private BigDecimal contractAmount;
    private LocalDateTime signDate;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status; // 审批状态：DRAFT-草稿，PENDING-待审批，APPROVED-已审批，REJECTED-已拒绝
    private String executionStatus; // 执行状态：PENDING_EXECUTION-待履约，EXECUTING-履约中，COMPLETED-已履约，INVALID-已失效，ARCHIVED-已归档
    private Integer isManualModify; // 是否人工修改（0-否，1-是）
    private Long deptId; // 申请人部门ID
    private Long empId; // 申请人ID
    private String empCode; // 申请人工号（用于显示，不持久化）
    private String empName; // 申请人姓名（用于显示，不持久化）
    private String deptName; // 申请人部门名称（用于显示，不持久化）
    private String empPhone; // 申请人手机号（用于显示，不持久化）
    private Long templateConfigId; // 模板配置ID
    private String mainAttachId; // 主附件ID（用于附件关联）
    private String remark;
    private String createUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

