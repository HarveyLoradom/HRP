package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 预算主体关联科室表实体
 */
@Data
public class BudgetSubjectDept implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long subjectId; // 预算主体ID
    private Long deptId; // 关联的科室ID
    private String deptCode; // 关联的科室编码
    private String deptName; // 关联的科室名称
    private String createUser;
    private LocalDateTime createTime;
}

