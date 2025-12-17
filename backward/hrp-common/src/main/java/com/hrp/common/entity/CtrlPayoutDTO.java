package com.hrp.common.entity;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 报账单DTO（包含明细、发票、支付清单）
 */
@Data
public class CtrlPayoutDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private CtrlPayout payout; // 主表信息
    private List<CtrlPayoutDetail> details; // 支付明细（报账单使用）
    private List<CtrlPayoutInvoice> invoices; // 发票列表（报账单使用）
    private List<CtrlPayoutPayment> payments; // 支付清单（报账单使用）
    private List<CtrlPayoutApproval> approvals; // 审批记录
}













