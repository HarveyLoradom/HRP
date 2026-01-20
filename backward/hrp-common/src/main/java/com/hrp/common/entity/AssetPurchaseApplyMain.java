package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 采购申请主表实体
 */
@Data
public class AssetPurchaseApplyMain implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String applyNo;
    private String applyDeptId;
    private Long applyEmpId;
    private LocalDateTime applyTime;
    private LocalDate demandDate;
    private String applyReason;
    private BigDecimal applyMoney; // 申请总金额（所有明细的总价之和，自动计算）
    private String status; // 关联sys_code的APPLY_STATUS，值：DRAFT, PENDING, APPROVED, REJECTED, WITHDRAWN
    private String createUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long templateConfigId;
    private String mainAttachId; // 主附件ID（时间戳，用于附件关联和文件夹命名）
    // 扩展字段，用于前端显示（不映射到数据库）
    private String applyEmpName;
    private String applyEmpCode; // 申请人工号（关联查询字段）
    private String applyDeptName;
    private String applyDeptCode; // 申请部门编码（关联查询字段）
}
