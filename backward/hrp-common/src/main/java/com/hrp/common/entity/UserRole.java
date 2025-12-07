package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户角色关联实体
 */
@Data
public class UserRole implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String userId;
    private Long roleId;
    private LocalDateTime createTime;
}

