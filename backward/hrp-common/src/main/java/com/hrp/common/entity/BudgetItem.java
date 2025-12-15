package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 预算项目实体
 */
@Data
public class BudgetItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long itemId;
    private String itemCode;
    private String itemName;
    private Long categoryId;
    private String categoryCode;
    private String accountSubject; // 关联会计科目
    private Long isCentralized; // 是否归口管理：0-否，1-是
    private Long allowAdjust; // 是否允许调整：0-否，1-是
    private String itemDesc;
    private Long isStop;
    private String createUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}








