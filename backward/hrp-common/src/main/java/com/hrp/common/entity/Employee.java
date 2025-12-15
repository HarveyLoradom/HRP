package com.hrp.common.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.hrp.common.util.LocalDateTimeDeserializer;
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
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime empBirthday;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String empEmail;
    private String empPhone;
    private Long deptId;
    private String deptCode;
    private String deptName; // 部门名称（关联查询）
    private String createUser;
    private Long isStop;
    private Long empTypeId;
    private String empTypeName; // 职工类型名称（关联查询）
    private Long userType; // 用户类型ID（从sys_user表关联查询）
    private String userTypeName; // 用户类型名称（关联查询）
    private String bankAccount; // 银行卡号
    private String bankName; // 银行名称
    private String photo; // 照片路径
}
