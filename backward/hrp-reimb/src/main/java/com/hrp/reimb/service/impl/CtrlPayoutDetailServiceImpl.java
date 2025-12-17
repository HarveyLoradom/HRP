package com.hrp.reimb.service.impl;

import com.hrp.reimb.mapper.CtrlPayoutDetailMapper;
import com.hrp.reimb.service.CtrlPayoutDetailService;
import com.hrp.common.entity.CtrlPayoutDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CtrlPayoutDetailServiceImpl implements CtrlPayoutDetailService {

    @Autowired
    private CtrlPayoutDetailMapper ctrlPayoutDetailMapper;

    @Override
    public List<CtrlPayoutDetail> getByPayoutId(Long payoutId) {
        return ctrlPayoutDetailMapper.selectByPayoutId(payoutId);
    }

    @Override
    @Transactional
    public boolean save(CtrlPayoutDetail detail) {
        return ctrlPayoutDetailMapper.insert(detail) > 0;
    }

    @Override
    @Transactional
    public boolean saveBatch(List<CtrlPayoutDetail> details) {
        for (CtrlPayoutDetail detail : details) {
            if (ctrlPayoutDetailMapper.insert(detail) <= 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    @Transactional
    public boolean update(CtrlPayoutDetail detail) {
        return ctrlPayoutDetailMapper.updateById(detail) > 0;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        return ctrlPayoutDetailMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional
    public boolean deleteByPayoutId(Long payoutId) {
        return ctrlPayoutDetailMapper.deleteByPayoutId(payoutId) > 0;
    }
}













