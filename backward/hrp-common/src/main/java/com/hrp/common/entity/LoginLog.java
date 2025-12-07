package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录日志实体
 */
@Data
public class LoginLog implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String account;
    private String ip;
    private Integer status;
    private String message;
    private LocalDateTime loginTime;
}
