package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 科室实体
 */
@Data
public class Clinic implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long clinicId;
    private String clinicCode;
    private String clinicName;
    private Long deptId;
    private String clinicType;
    private String clinicPhone;
    private Long clinicManagerId;
    private Long isStop;
    private String createUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

