package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 预算项目实体
 */
@Data
public class BudgetItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long itemId;
    private String itemCode;
    private String itemName;
    private String budgetYear; // 预算年度（必填）
    private Long categoryId;
    private String categoryCode;
    private String categoryType; // INCOME-收入预算，EXPENSE-支出预算
    private Long parentCategoryId; // 上级分类ID（支持多级分类）
    private String parentCategoryCode;
    private String accountSubject; // 关联会计科目
    private Long isCentralized; // 是否归口管理：0-否，1-是
    private Long allowAdjust; // 是否允许调整：0-否，1-是
    private String itemDesc;
    private Long isStop;
    private String createUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 分配的主体列表
    private List<BudgetSubject> assignedSubjects;
    private List<Long> assignedSubjectIds; // 用于前端传递
}













