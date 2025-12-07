package com.hrp.reimb.service.impl;

import com.hrp.reimb.mapper.CtrlPayoutMapper;
import com.hrp.reimb.service.CtrlPayoutService;
import com.hrp.common.entity.CtrlPayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CtrlPayoutServiceImpl implements CtrlPayoutService {

    @Autowired
    private CtrlPayoutMapper ctrlPayoutMapper;

    @Override
    public List<CtrlPayout> getMyPayouts(Long empId) {
        return ctrlPayoutMapper.selectByEmpId(empId);
    }

    @Override
    public List<CtrlPayout> getPayoutsByStatus(String status) {
        return ctrlPayoutMapper.selectByStatus(status);
    }

    @Override
    public CtrlPayout getById(Long id) {
        return ctrlPayoutMapper.selectById(id);
    }

    @Override
    public CtrlPayout getByBillcode(String billcode) {
        return ctrlPayoutMapper.selectByBillcode(billcode);
    }

    @Override
    @Transactional
    public boolean save(CtrlPayout ctrlPayout) {
        if (ctrlPayout.getPayoutBillcode() == null || ctrlPayout.getPayoutBillcode().isEmpty()) {
            ctrlPayout.setPayoutBillcode("PAYOUT" + System.currentTimeMillis());
        }
        if (ctrlPayout.getApplyDate() == null) {
            ctrlPayout.setApplyDate(LocalDateTime.now());
        }
        if (ctrlPayout.getStatus() == null || ctrlPayout.getStatus().isEmpty()) {
            ctrlPayout.setStatus("PENDING");
        }
        return ctrlPayoutMapper.insert(ctrlPayout) > 0;
    }

    @Override
    @Transactional
    public boolean update(CtrlPayout ctrlPayout) {
        return ctrlPayoutMapper.updateById(ctrlPayout) > 0;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        return ctrlPayoutMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional
    public boolean submit(Long payoutId) {
        CtrlPayout payout = ctrlPayoutMapper.selectById(payoutId);
        if (payout == null) {
            return false;
        }
        payout.setStatus("PENDING");
        return ctrlPayoutMapper.updateById(payout) > 0;
    }
}

