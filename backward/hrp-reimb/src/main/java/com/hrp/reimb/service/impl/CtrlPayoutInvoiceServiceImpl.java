package com.hrp.reimb.service.impl;

import com.hrp.reimb.mapper.CtrlPayoutInvoiceMapper;
import com.hrp.reimb.service.CtrlPayoutInvoiceService;
import com.hrp.common.entity.CtrlPayoutInvoice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CtrlPayoutInvoiceServiceImpl implements CtrlPayoutInvoiceService {

    @Autowired
    private CtrlPayoutInvoiceMapper ctrlPayoutInvoiceMapper;

    @Override
    public List<CtrlPayoutInvoice> getByPayoutId(Long payoutId) {
        return ctrlPayoutInvoiceMapper.selectByPayoutId(payoutId);
    }

    @Override
    @Transactional
    public boolean save(CtrlPayoutInvoice invoice) {
        return ctrlPayoutInvoiceMapper.insert(invoice) > 0;
    }

    @Override
    @Transactional
    public boolean saveBatch(List<CtrlPayoutInvoice> invoices) {
        for (CtrlPayoutInvoice invoice : invoices) {
            if (ctrlPayoutInvoiceMapper.insert(invoice) <= 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    @Transactional
    public boolean update(CtrlPayoutInvoice invoice) {
        return ctrlPayoutInvoiceMapper.updateById(invoice) > 0;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        return ctrlPayoutInvoiceMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional
    public boolean deleteByPayoutId(Long payoutId) {
        return ctrlPayoutInvoiceMapper.deleteByPayoutId(payoutId) > 0;
    }
}













