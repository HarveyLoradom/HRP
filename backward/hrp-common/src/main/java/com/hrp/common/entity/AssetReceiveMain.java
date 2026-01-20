package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 资产领用主记录实体
 */
@Data
public class AssetReceiveMain implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String receiveNo;
    private String applyEmpId;
    private Long deptId;
    private LocalDate receiveDate;
    private String receiveReason;
    private LocalDateTime createTime;
    private String operatorCode;
    private LocalDateTime updateTime;
    // 扩展字段，用于前端显示（关联查询结果）
    private String applyEmpName;
    private String applyEmpCode;
    private String deptName;
    private String operatorName;
    // 明细列表（不映射到数据库）
    private java.util.List<AssetReceiveDetail> details;
}
