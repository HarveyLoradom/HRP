package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 采购需求明细实体
 */
@Data
public class ProcurementRequirementDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long detailId;
    private Long requirementId;
    private String assetName;
    private String assetType;
    private String assetSpec;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private String purpose;
    private String remark;
    private LocalDateTime createTime;
}



