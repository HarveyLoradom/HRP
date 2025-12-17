package com.hrp.reimb.service;

import com.hrp.common.entity.CtrlPayoutInvoice;

import java.util.List;

/**
 * 发票服务接口
 */
public interface CtrlPayoutInvoiceService {
    List<CtrlPayoutInvoice> getByPayoutId(Long payoutId);
    boolean save(CtrlPayoutInvoice invoice);
    boolean saveBatch(List<CtrlPayoutInvoice> invoices);
    boolean update(CtrlPayoutInvoice invoice);
    boolean delete(Long id);
    boolean deleteByPayoutId(Long payoutId);
}













