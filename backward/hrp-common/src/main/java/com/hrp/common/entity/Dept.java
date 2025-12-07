package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 部门实体
 */
@Data
public class Dept implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long deptId;
    private String deptCode;
    private String deptName;
    private String superDeptCode;
    private Long deptLevel;
    private String deptPhone;
    private Long deptManagerId;
    private Long isStop;
    private String createUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
