package com.hrp.reimb.service;

import com.hrp.common.entity.CtrlPayoutApproval;

import java.util.List;

/**
 * 审批记录服务接口
 */
public interface CtrlPayoutApprovalService {
    List<CtrlPayoutApproval> getByPayoutId(Long payoutId);
    boolean save(CtrlPayoutApproval approval);
    boolean delete(Long id);
}








