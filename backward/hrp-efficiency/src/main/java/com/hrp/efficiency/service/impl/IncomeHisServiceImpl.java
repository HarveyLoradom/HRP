package com.hrp.efficiency.service.impl;

import com.hrp.efficiency.mapper.IncomeHisMapper;
import com.hrp.efficiency.service.IncomeHisService;
import com.hrp.common.entity.IncomeHis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class IncomeHisServiceImpl implements IncomeHisService {

    @Autowired
    private IncomeHisMapper incomeHisMapper;

    @Override
    public List<IncomeHis> getAll() {
        return incomeHisMapper.selectAll();
    }

    @Override
    public List<IncomeHis> getByDateRange(LocalDate startDate, LocalDate endDate) {
        return incomeHisMapper.selectByDateRange(startDate, endDate);
    }

    @Override
    public IncomeHis getById(Long id) {
        return incomeHisMapper.selectById(id);
    }

    @Override
    @Transactional
    public boolean save(IncomeHis incomeHis) {
        if (incomeHis.getIncomeNo() == null || incomeHis.getIncomeNo().isEmpty()) {
            incomeHis.setIncomeNo("HIS" + System.currentTimeMillis());
        }
        if (incomeHis.getIncomeDate() == null) {
            incomeHis.setIncomeDate(LocalDate.now());
        }
        return incomeHisMapper.insert(incomeHis) > 0;
    }

    @Override
    @Transactional
    public boolean update(IncomeHis incomeHis) {
        return incomeHisMapper.updateById(incomeHis) > 0;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        return incomeHisMapper.deleteById(id) > 0;
    }
}

