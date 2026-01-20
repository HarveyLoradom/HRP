package com.hrp.cost.service;

import com.hrp.common.entity.CostCycle;
import com.hrp.common.entity.Result;
import java.util.List;

/**
 * 成本周期服务接口
 */
public interface CostCycleService {
    CostCycle getById(Long id);
    CostCycle getByCode(String cycleCode);
    List<CostCycle> getAll(String cycleCode, String cycleName, String cycleType, Integer status);
    CostCycle save(CostCycle costCycle);
    CostCycle update(CostCycle costCycle);
    boolean delete(Long id);
}

