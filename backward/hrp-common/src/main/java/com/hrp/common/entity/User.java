package com.hrp.common.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.hrp.common.util.LocalDateTimeDeserializer;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String account;
    private String name;
    private Long type;
    private String phone;
    private String password;
    private String createUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long isStop;
    // 锁定状态（从sys_user_login表关联查询）
    private Integer locked;
    // 职工类型ID（对应sys_emp表的emp_type_id）
    private Long empTypeId;
    // 员工相关字段（用于创建sys_emp记录）
    private Long empSex; // 性别：1-男，2-女
    private String idCard; // 身份证号
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private java.time.LocalDateTime empBirthday; // 出生日期
    private String empEmail; // 邮箱
    private Long deptId; // 部门ID
    private String deptCode; // 部门编码（关联查询字段）
    private String deptName; // 部门名称（关联查询字段）
}
