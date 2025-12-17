package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import com.hrp.common.entity.Dept;

/**
 * 预算主体实体
 */
@Data
public class BudgetSubject implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private String subjectType; // CLINIC-临床科室，ADMIN-行政部门，TECH-医技科室，FUNC-职能部门
    // 归口部门
    private Long manageDeptId;
    private String manageDeptCode;
    private String manageDeptName;
    // 归口负责人
    private Long manageEmpId;
    private String manageEmpCode;
    private String manageEmpName;
    private Long isStop;
    private String createUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 关联的科室列表（多选）
    private List<Dept> relatedDepts;
    private List<Long> relatedDeptIds; // 用于前端传递
}













