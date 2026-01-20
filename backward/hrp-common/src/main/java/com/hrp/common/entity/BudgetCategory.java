package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 预算项目分类实体
 */
@Data
public class BudgetCategory implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long categoryId;
    private String categoryCode;
    private String categoryName;
    private String budgetYear; // 预算年度（必填）
    private String categoryType; // INCOME-收入预算，EXPENSE-支出预算，SPECIAL-专项预算
    private Long parentCategoryId;
    private String parentCategoryCode;
    private Integer categoryLevel; // 1-一级分类，2-二级分类
    private Long isStop;
    private String createUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}













