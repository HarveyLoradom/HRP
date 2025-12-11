package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户和员工关联信息DTO
 */
@Data
public class UserWithEmployee implements Serializable {
    private static final long serialVersionUID = 1L;

    // 用户信息
    private String userId;
    private String account;
    private String userName;
    private Long userType;
    private String phone;
    private Long isStop;
    private Integer locked;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 员工信息
    private Long empId;
    private String empCode;
    private String empName;
    private Long empSex;
    private String empPhone;
    private String empEmail;
    private String deptCode;
    private String deptName;
}

