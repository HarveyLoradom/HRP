package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 发票实体
 */
@Data
public class CtrlPayoutInvoice implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long invoiceId;
    private Long payoutId;
    private String invoiceCode; // 发票代码
    private String invoiceNumber; // 发票号码
    private LocalDateTime invoiceDate; // 发票日期
    private BigDecimal invoiceAmount; // 发票金额
    private String invoiceType; // 发票类型：VAT-增值税发票，COMMON-普通发票
    private BigDecimal taxAmount; // 税额
    private Long attachmentId; // 发票附件ID（关联sys_attachment表）
    private String remark;
}

