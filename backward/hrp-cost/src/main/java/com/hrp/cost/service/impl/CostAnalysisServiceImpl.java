package com.hrp.cost.service.impl;

import com.hrp.cost.mapper.CostAnalysisMapper;
import com.hrp.cost.service.CostAnalysisService;
import com.hrp.common.entity.CostAnalysis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CostAnalysisServiceImpl implements CostAnalysisService {

    @Autowired
    private CostAnalysisMapper costAnalysisMapper;

    @Override
    public List<CostAnalysis> getAll() {
        return costAnalysisMapper.selectAll();
    }

    @Override
    public CostAnalysis getById(Long id) {
        return costAnalysisMapper.selectById(id);
    }

    @Override
    @Transactional
    public boolean save(CostAnalysis costAnalysis) {
        if (costAnalysis.getAnalysisNo() == null || costAnalysis.getAnalysisNo().isEmpty()) {
            costAnalysis.setAnalysisNo("ANALYSIS" + System.currentTimeMillis());
        }
        return costAnalysisMapper.insert(costAnalysis) > 0;
    }

    @Override
    @Transactional
    public boolean update(CostAnalysis costAnalysis) {
        return costAnalysisMapper.updateById(costAnalysis) > 0;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        return costAnalysisMapper.deleteById(id) > 0;
    }
}

