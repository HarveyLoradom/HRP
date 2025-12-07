package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 职工实体
 */
@Data
public class Employee implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long empId;
    private String empCode;
    private String empName;
    private Long empSex;
    private String idCard;
    private LocalDateTime empBirthday;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String empEmail;
    private String empPhone;
    private Long deptId;
    private String deptCode;
    private String createUser;
    private Long isStop;
    private Long empTypeId;
}
