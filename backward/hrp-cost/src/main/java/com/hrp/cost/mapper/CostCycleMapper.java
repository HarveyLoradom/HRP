package com.hrp.cost.mapper;

import com.hrp.common.entity.CostCycle;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 成本周期数据访问接口
 */
public interface CostCycleMapper {
    CostCycle selectById(@Param("id") Long id);
    CostCycle selectByCode(@Param("cycleCode") String cycleCode);
    List<CostCycle> selectAll();
    List<CostCycle> selectByConditions(@Param("cycleCode") String cycleCode,
                                       @Param("cycleName") String cycleName,
                                       @Param("cycleType") String cycleType,
                                       @Param("status") Integer status);
    int insert(CostCycle costCycle);
    int updateById(CostCycle costCycle);
    int deleteById(@Param("id") Long id);
}

