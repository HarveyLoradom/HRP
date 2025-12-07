package com.hrp.cost.service.impl;

import com.hrp.cost.mapper.CostReportMapper;
import com.hrp.cost.service.CostReportService;
import com.hrp.common.entity.CostReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CostReportServiceImpl implements CostReportService {

    @Autowired
    private CostReportMapper costReportMapper;

    @Override
    public List<CostReport> getAll() {
        return costReportMapper.selectAll();
    }

    @Override
    public List<CostReport> getByPeriod(String reportPeriod) {
        return costReportMapper.selectByPeriod(reportPeriod);
    }

    @Override
    public CostReport getById(Long id) {
        return costReportMapper.selectById(id);
    }

    @Override
    @Transactional
    public boolean save(CostReport costReport) {
        if (costReport.getReportNo() == null || costReport.getReportNo().isEmpty()) {
            costReport.setReportNo("COST" + System.currentTimeMillis());
        }
        return costReportMapper.insert(costReport) > 0;
    }

    @Override
    @Transactional
    public boolean update(CostReport costReport) {
        return costReportMapper.updateById(costReport) > 0;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        return costReportMapper.deleteById(id) > 0;
    }
}

