package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 密码修改日志实体
 */
@Data
public class PasswordChangeLog implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long logId;
    private String userId; // 用户ID
    private Long empId; // 职工ID
    private String empCode; // 工号
    private String empName; // 姓名
    private String oldPassword; // 原密码（明文）
    private String newPassword; // 新密码（明文）
    private LocalDateTime changeTime; // 修改时间
    private String changeIp; // 修改IP地址
    private String changeUser; // 操作人
    private String remark; // 备注
}

