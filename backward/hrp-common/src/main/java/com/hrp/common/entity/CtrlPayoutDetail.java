package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 报账明细实体
 */
@Data
public class CtrlPayoutDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long payoutId;
    private String itemName;
    private String itemType;
    private BigDecimal amount;
    private String invoiceNo;
    private LocalDateTime invoiceDate;
    private String remark;
}

