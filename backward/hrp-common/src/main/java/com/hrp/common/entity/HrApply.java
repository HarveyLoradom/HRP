package com.hrp.common.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.hrp.common.util.LocalDateTimeDeserializer;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 业务申请表（补卡/请假/加班申请）实体
 */
@Data
public class HrApply implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long applyId;
    private String applyNo;
    private Long empId;
    private String empCode; // 员工编码（关联查询）
    private String empName; // 员工姓名（关联查询）
    private String empPhone; // 手机号（从sys_emp表关联获取）
    private Long deptId; // 申请人部门ID（冗余字段，方便查询）
    private String deptName; // 申请人部门名称（冗余字段，方便查询）
    private String hrApplyType; // 申请类型
    private String hrApplySubType; // 申请子类型
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime endTime;
    private Long supplementId; // 打卡记录编号
    private String applyDay; // 申请天数
    private String applyReason;
    private String mainAttachId; // 主附件ID（用于附件关联）
    private Integer isNurse; // 是否护士：0-否，1-是
    private String status;
    private Long templateConfigId; // 模板配置ID
    private Long processDefinitionId; // 流程定义ID
    private Long processInstanceId; // 流程实例ID
    private String remark; // 备注
    private String createUser; // 创建人
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime updateTime;
}

