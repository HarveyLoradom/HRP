package com.hrp.reimb.service;

import com.hrp.common.entity.CtrlPayoutDetail;

import java.util.List;

/**
 * 报账明细服务接口
 */
public interface CtrlPayoutDetailService {
    List<CtrlPayoutDetail> getByPayoutId(Long payoutId);
    boolean save(CtrlPayoutDetail detail);
    boolean saveBatch(List<CtrlPayoutDetail> details);
    boolean update(CtrlPayoutDetail detail);
    boolean delete(Long id);
    boolean deleteByPayoutId(Long payoutId);
}













