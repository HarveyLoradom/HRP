package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户类型菜单关联实体
 */
@Data
public class UserTypeMenu implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String userType; // 用户类型（对应sys_code的code_value）
    private Long menuId;
    private LocalDateTime createTime;
}

