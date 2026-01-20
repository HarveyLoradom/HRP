package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

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
    private String remark;
}

