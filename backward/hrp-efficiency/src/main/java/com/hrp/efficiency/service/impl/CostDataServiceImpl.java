package com.hrp.efficiency.service.impl;

import com.hrp.efficiency.mapper.CostDataMapper;
import com.hrp.efficiency.service.CostDataService;
import com.hrp.common.entity.CostData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class CostDataServiceImpl implements CostDataService {

    @Autowired
    private CostDataMapper costDataMapper;

    @Override
    public List<CostData> getAll() {
        return costDataMapper.selectAll();
    }

    @Override
    public List<CostData> getByDateRange(LocalDate startDate, LocalDate endDate) {
        return costDataMapper.selectByDateRange(startDate, endDate);
    }

    @Override
    public CostData getById(Long id) {
        return costDataMapper.selectById(id);
    }

    @Override
    @Transactional
    public boolean save(CostData costData) {
        if (costData.getCostNo() == null || costData.getCostNo().isEmpty()) {
            costData.setCostNo("COST" + System.currentTimeMillis());
        }
        if (costData.getCostDate() == null) {
            costData.setCostDate(LocalDate.now());
        }
        return costDataMapper.insert(costData) > 0;
    }

    @Override
    @Transactional
    public boolean update(CostData costData) {
        return costDataMapper.updateById(costData) > 0;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        return costDataMapper.deleteById(id) > 0;
    }
}

