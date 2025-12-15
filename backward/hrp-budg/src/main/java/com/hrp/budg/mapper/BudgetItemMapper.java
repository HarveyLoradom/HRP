package com.hrp.budg.mapper;

import com.hrp.common.entity.BudgetItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 预算项目数据访问接口
 */
public interface BudgetItemMapper {
    BudgetItem selectById(@Param("id") Long id);
    BudgetItem selectByCode(@Param("code") String code);
    List<BudgetItem> selectAll();
    List<BudgetItem> selectByCategoryId(@Param("categoryId") Long categoryId);
    int insert(BudgetItem budgetItem);
    int updateById(BudgetItem budgetItem);
    int deleteById(@Param("id") Long id);
}








