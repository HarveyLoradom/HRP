package com.hrp.reimb.service.impl;

import com.hrp.reimb.mapper.CtrlPayoutPaymentMapper;
import com.hrp.reimb.service.CtrlPayoutPaymentService;
import com.hrp.common.entity.CtrlPayoutPayment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CtrlPayoutPaymentServiceImpl implements CtrlPayoutPaymentService {

    @Autowired
    private CtrlPayoutPaymentMapper ctrlPayoutPaymentMapper;

    @Override
    public List<CtrlPayoutPayment> getByPayoutId(Long payoutId) {
        return ctrlPayoutPaymentMapper.selectByPayoutId(payoutId);
    }

    @Override
    @Transactional
    public boolean save(CtrlPayoutPayment payment) {
        return ctrlPayoutPaymentMapper.insert(payment) > 0;
    }

    @Override
    @Transactional
    public boolean saveBatch(List<CtrlPayoutPayment> payments) {
        for (CtrlPayoutPayment payment : payments) {
            if (ctrlPayoutPaymentMapper.insert(payment) <= 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    @Transactional
    public boolean update(CtrlPayoutPayment payment) {
        return ctrlPayoutPaymentMapper.updateById(payment) > 0;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        return ctrlPayoutPaymentMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional
    public boolean deleteByPayoutId(Long payoutId) {
        return ctrlPayoutPaymentMapper.deleteByPayoutId(payoutId) > 0;
    }
}




