package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 固定资产实体
 */
@Data
public class FixedAsset implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long assetId;
    private String assetNo;
    private String assetName;
    private String assetType;
    private String assetCategory;
    private LocalDateTime purchaseDate;
    private BigDecimal purchaseAmount;
    private BigDecimal currentValue;
    private Long deptId;
    private Long managerId;
    private String location;
    private String status;
    private String remark;
    private String createUser;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

