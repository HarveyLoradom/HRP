package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 采购申请明细表实体
 */
@Data
public class AssetPurchaseApplyDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long applyId;
    private String assetCode;
    private String assetName;
    private String spec;
    private String manufacturer;
    private String unit;
    private Integer applyQuantity;
    private BigDecimal price;
    private BigDecimal totalPrice;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
