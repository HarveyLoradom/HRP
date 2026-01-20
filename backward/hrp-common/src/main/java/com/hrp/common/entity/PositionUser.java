package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 岗位人员关联实体
 */
@Data
public class PositionUser implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long positionId;
    private String userId;
    private LocalDateTime createTime;
}

