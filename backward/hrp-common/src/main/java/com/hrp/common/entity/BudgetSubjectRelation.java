package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 预算主体关联表实体（支持一个主体关联多个主体）
 */
@Data
public class BudgetSubjectRelation implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long relationId;
    private Long subjectId; // 预算主体ID
    private Long relatedSubjectId; // 关联的主体ID（多选的主体）
    private String relatedSubjectCode;
    private String relatedSubjectName;
    private String createUser;
    private LocalDateTime createTime;
}

