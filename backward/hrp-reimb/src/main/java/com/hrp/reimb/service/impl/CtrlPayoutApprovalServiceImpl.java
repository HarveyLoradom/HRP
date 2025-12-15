package com.hrp.reimb.service.impl;

import com.hrp.reimb.mapper.CtrlPayoutApprovalMapper;
import com.hrp.reimb.service.CtrlPayoutApprovalService;
import com.hrp.common.entity.CtrlPayoutApproval;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CtrlPayoutApprovalServiceImpl implements CtrlPayoutApprovalService {

    @Autowired
    private CtrlPayoutApprovalMapper ctrlPayoutApprovalMapper;

    @Override
    public List<CtrlPayoutApproval> getByPayoutId(Long payoutId) {
        return ctrlPayoutApprovalMapper.selectByPayoutId(payoutId);
    }

    @Override
    @Transactional
    public boolean save(CtrlPayoutApproval approval) {
        return ctrlPayoutApprovalMapper.insert(approval) > 0;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        return ctrlPayoutApprovalMapper.deleteById(id) > 0;
    }
}








