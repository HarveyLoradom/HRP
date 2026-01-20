package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户登录信息实体（用于记录登录失败次数和锁定状态）
 */
@Data
public class UserLogin implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String userId;
    private String account;
    private Integer locked;
    private LocalDateTime lockTime;
    private Integer loginFailCount;
    private LocalDateTime lastLoginTime;
    private LocalDateTime updateTime;
}
