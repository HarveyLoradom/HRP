package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 字典实体
 */
@Data
public class Code implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String codeValue;
    private String codeName;
    private String codeType;
    private String codeTypeName;
    private Long isStop;
    private String createUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
