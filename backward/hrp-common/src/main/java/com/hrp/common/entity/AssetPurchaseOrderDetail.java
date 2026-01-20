package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 采购单明细表实体
 */
@Data
public class AssetPurchaseOrderDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long orderId;
    private Long applyDetailId;
    private Long assetCode;
    private String assetName;
    private String spec;
    private String manufacturer;
    private String unit;
    private Integer applyQuantity;
    private Integer actualQuantity;
    private BigDecimal expectedPrice;
    private BigDecimal expectedAmount;
    private BigDecimal actualPrice;
    private BigDecimal totalActualPrice;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
