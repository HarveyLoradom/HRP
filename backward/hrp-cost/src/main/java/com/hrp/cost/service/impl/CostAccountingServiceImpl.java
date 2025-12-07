package com.hrp.cost.service.impl;

import com.hrp.cost.mapper.CostAccountingMapper;
import com.hrp.cost.service.CostAccountingService;
import com.hrp.common.entity.CostAccounting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CostAccountingServiceImpl implements CostAccountingService {

    @Autowired
    private CostAccountingMapper costAccountingMapper;

    @Override
    public List<CostAccounting> getAll() {
        return costAccountingMapper.selectAll();
    }

    @Override
    public List<CostAccounting> getByPeriod(String accountingPeriod) {
        return costAccountingMapper.selectByPeriod(accountingPeriod);
    }

    @Override
    public CostAccounting getById(Long id) {
        return costAccountingMapper.selectById(id);
    }

    @Override
    @Transactional
    public boolean save(CostAccounting costAccounting) {
        if (costAccounting.getAccountingNo() == null || costAccounting.getAccountingNo().isEmpty()) {
            costAccounting.setAccountingNo("ACCOUNTING" + System.currentTimeMillis());
        }
        return costAccountingMapper.insert(costAccounting) > 0;
    }

    @Override
    @Transactional
    public boolean update(CostAccounting costAccounting) {
        return costAccountingMapper.updateById(costAccounting) > 0;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        return costAccountingMapper.deleteById(id) > 0;
    }
}

