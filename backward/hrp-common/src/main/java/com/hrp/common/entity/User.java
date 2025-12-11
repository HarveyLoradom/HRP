package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String account;
    private String name;
    private Long type;
    private String phone;
    private String password;
    private String createUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long isStop;
    // 锁定状态（从sys_user_login表关联查询）
    private Integer locked;
}
