package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 岗位实体
 */
@Data
public class Position implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long positionId;
    private String positionCode;
    private String positionName;
    private Long isStop;
    private String createUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 关联的人员列表（用于前端显示）
    private List<User> users;
}

