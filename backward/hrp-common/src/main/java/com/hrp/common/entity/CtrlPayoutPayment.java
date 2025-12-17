package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付清单实体
 */
@Data
public class CtrlPayoutPayment implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long paymentId;
    private Long payoutId;
    private BigDecimal paymentAmount; // 支付金额
    private String paymentObject; // 支付对象（收款人）
    private String paymentMethod; // 支付方式：BANK-银行转账，CASH-现金，CHECK-支票，OTHER-其他
    private String bankName; // 银行名称
    private String bankAccount; // 银行账号
    private String accountName; // 账户名称
    private LocalDateTime paymentDate; // 支付日期
    private String remark;
}













