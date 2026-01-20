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
    private String deptManagerName; // 部门负责人姓名（关联查询）
    private String deptManagerCode; // 部门负责人工号（关联查询）
    private Long nurseManagerId; // 护士长ID
    private String nurseManagerName; // 护士长姓名（关联查询）
    private String nurseManagerCode; // 护士长工号（关联查询）
    private Long vicePresidentId; // 分管院长ID
    private String vicePresidentName; // 分管院长姓名（关联查询）
    private String vicePresidentCode; // 分管院长工号（关联查询）
    private Long isStop;
    private String createUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
