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
}
