package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户菜单关联实体
 */
@Data
public class UserMenu implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String userId;
    private Long menuId;
    private LocalDateTime createTime;
}



