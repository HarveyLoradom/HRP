package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 资产审批明细实体
 */
@Data
public class AssetApprovalDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long detailId;
    private Long approvalId;
    private Long assetId;
    private String assetNo;
    private String assetName;
    private String assetType;
    private Long originalDeptId;
    private String originalDeptName;
    private Long targetDeptId;
    private String targetDeptName;
    private String originalLocation;
    private String targetLocation;
    private BigDecimal originalValue;
    private BigDecimal newValue;
    private String disposalMethod;
    private BigDecimal disposalAmount;
    private String inventoryDiff;
    private String changeContent;
    private String remark;
    private LocalDateTime createTime;
}



