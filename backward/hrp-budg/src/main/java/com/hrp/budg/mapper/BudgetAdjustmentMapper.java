package com.hrp.budg.mapper;

import com.hrp.common.entity.BudgetAdjustment;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 预算调整数据访问接口
 */
public interface BudgetAdjustmentMapper {
    BudgetAdjustment selectById(@Param("id") Long id);
    BudgetAdjustment selectByNo(@Param("no") String no);
    List<BudgetAdjustment> selectAll();
    List<BudgetAdjustment> selectPage(@Param("offset") int offset, @Param("size") int size, @Param("params") Map<String, Object> params);
    long countByConditions(@Param("params") Map<String, Object> params);
    List<BudgetAdjustment> selectByItemId(@Param("itemId") Long itemId);
    int insert(BudgetAdjustment budgetAdjustment);
    int updateById(BudgetAdjustment budgetAdjustment);
    int deleteById(@Param("id") Long id);
}

