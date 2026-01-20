package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 预算项目主体分配表实体
 */
@Data
public class BudgetItemSubject implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long itemId; // 预算项目ID
    private Long subjectId; // 预算主体ID
    private String subjectCode;
    private String subjectName;
    private String createUser;
    private LocalDateTime createTime;
}

