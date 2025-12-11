package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 预算主体实体
 */
@Data
public class BudgetSubject implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private String subjectType; // CLINIC-临床科室，ADMIN-行政部门，TECH-医技科室
    private Long parentSubjectId;
    private String parentSubjectCode;
    private Long deptId;
    private String deptCode;
    private Long managerId;
    private String managerCode;
    private String managerName;
    private Integer subjectLevel;
    private Long isStop;
    private String createUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}




