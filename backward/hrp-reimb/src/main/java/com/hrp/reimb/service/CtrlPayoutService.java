package com.hrp.reimb.service;

import com.hrp.common.entity.CtrlPayout;

import java.util.List;

/**
 * 报账服务接口
 */
public interface CtrlPayoutService {
    List<CtrlPayout> getMyPayouts(Long empId);
    com.hrp.common.entity.PageResult<CtrlPayout> getMyPayoutsPage(Long empId, Long page, Long size);
    List<CtrlPayout> getPayoutsByStatus(String status);
    com.hrp.common.entity.PageResult<CtrlPayout> getPayoutsByStatusPage(String status, Long page, Long size);
    List<CtrlPayout> getMyApprovalPayouts(String userId);
    com.hrp.common.entity.PageResult<CtrlPayout> getMyApprovalPayoutsPage(String userId, Long page, Long size);
    List<CtrlPayout> getAllPayouts();
    com.hrp.common.entity.PageResult<CtrlPayout> getAllPayoutsPage(Long page, Long size);
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

