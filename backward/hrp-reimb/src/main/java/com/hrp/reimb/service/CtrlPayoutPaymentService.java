package com.hrp.reimb.service;

import com.hrp.common.entity.CtrlPayoutPayment;

import java.util.List;

/**
 * 支付清单服务接口
 */
public interface CtrlPayoutPaymentService {
    List<CtrlPayoutPayment> getByPayoutId(Long payoutId);
    boolean save(CtrlPayoutPayment payment);
    boolean saveBatch(List<CtrlPayoutPayment> payments);
    boolean update(CtrlPayoutPayment payment);
    boolean delete(Long id);
    boolean deleteByPayoutId(Long payoutId);
}













