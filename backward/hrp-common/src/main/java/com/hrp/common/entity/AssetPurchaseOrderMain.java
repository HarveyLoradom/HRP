package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 采购单主表实体
 */
@Data
public class AssetPurchaseOrderMain implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String orderNo;
    private String applyNo;
    private LocalDate orderDate;
    private Long supplierId;
    private String supplierName;
    private LocalDate deliveryDate;
    private Long operatorId;
    private Integer purchaseStatus; // 关联sys_code的PURCHASE_STATUS
    private Integer warehousingStatus; // 关联sys_code的WAREHOUSING_STATUS
    private String contractNo;
    private String paymentTerms;
    private String transportMode;
    private BigDecimal totalExpectedAmount;
    private BigDecimal totalActualAmount;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
