package com.hrp.reimb.service;

import com.hrp.common.entity.CtrlPayout;

import java.util.List;

/**
 * 报账服务接口
 */
public interface CtrlPayoutService {
    List<CtrlPayout> getMyPayouts(Long empId);
    List<CtrlPayout> getPayoutsByStatus(String status);
    List<CtrlPayout> getMyApprovalPayouts(String userId);
    List<CtrlPayout> getAllPayouts();
    CtrlPayout getById(Long id);
    CtrlPayout getByBillcode(String billcode);
    boolean save(CtrlPayout ctrlPayout);
    boolean update(CtrlPayout ctrlPayout);
    boolean delete(Long id);
    boolean submit(Long payoutId);
    boolean withdraw(Long payoutId);
    boolean approve(Long payoutId, String userId, String opinion);
    boolean reject(Long payoutId, String userId, String opinion);
    String getNextApprover(Long payoutId);
}

