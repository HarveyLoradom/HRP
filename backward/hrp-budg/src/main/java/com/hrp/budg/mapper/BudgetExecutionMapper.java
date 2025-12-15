package com.hrp.budg.mapper;

import com.hrp.common.entity.BudgetExecution;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 预算执行单数据访问接口
 */
public interface BudgetExecutionMapper {
    BudgetExecution selectById(@Param("id") Long id);
    BudgetExecution selectByNo(@Param("no") String no);
    List<BudgetExecution> selectAll();
    List<BudgetExecution> selectByBudgetId(@Param("budgetId") Long budgetId);
    List<BudgetExecution> selectByBusinessId(@Param("businessType") String businessType, @Param("businessId") Long businessId);
    int insert(BudgetExecution budgetExecution);
    int updateById(BudgetExecution budgetExecution);
    int deleteById(@Param("id") Long id);
}








