package com.hrp.cost.service.impl;

import com.hrp.cost.mapper.CostCycleMapper;
import com.hrp.cost.service.CostCycleService;
import com.hrp.common.entity.CostCycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CostCycleServiceImpl implements CostCycleService {
    
    @Autowired
    private CostCycleMapper costCycleMapper;

    @Override
    public CostCycle getById(Long id) {
        return costCycleMapper.selectById(id);
    }

    @Override
    public CostCycle getByCode(String cycleCode) {
        return costCycleMapper.selectByCode(cycleCode);
    }

    @Override
    public List<CostCycle> getAll(String cycleCode, String cycleName, String cycleType, Integer status) {
        return costCycleMapper.selectByConditions(cycleCode, cycleName, cycleType, status);
    }

    @Override
    @Transactional
    public CostCycle save(CostCycle costCycle) {
        // 检查周期编码是否已存在
        CostCycle exist = costCycleMapper.selectByCode(costCycle.getCycleCode());
        if (exist != null) {
            throw new RuntimeException("周期编码已存在：" + costCycle.getCycleCode());
        }
        int result = costCycleMapper.insert(costCycle);
        if (result > 0) {
            return costCycleMapper.selectById(costCycle.getCycleId());
        }
        return null;
    }

    @Override
    @Transactional
    public CostCycle update(CostCycle costCycle) {
        // 检查周期编码是否已存在（排除自己）
        CostCycle exist = costCycleMapper.selectByCode(costCycle.getCycleCode());
        if (exist != null && !exist.getCycleId().equals(costCycle.getCycleId())) {
            throw new RuntimeException("周期编码已存在：" + costCycle.getCycleCode());
        }
        int result = costCycleMapper.updateById(costCycle);
        if (result > 0) {
            return costCycleMapper.selectById(costCycle.getCycleId());
        }
        return null;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        return costCycleMapper.deleteById(id) > 0;
    }
}

