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
    /**
     * 分页查询项目预算（带筛选条件）
     */
    List<BudgetItem> selectPageByConditions(@Param("budgetYear") String budgetYear,
                                           @Param("categoryType") String categoryType,
                                           @Param("level1CategoryId") Long level1CategoryId,
                                           @Param("level2CategoryId") Long level2CategoryId,
                                           @Param("itemName") String itemName);
    /**
     * 根据二级分类编码查询最大项目编码
     */
    String selectMaxItemCodeByCategoryCode(@Param("categoryCode") String categoryCode);
    /**
     * 检查项目是否有预算编制或执行记录
     */
    int countBudgetsByItemId(@Param("itemId") Long itemId);
    int insert(BudgetItem budgetItem);
    int updateById(BudgetItem budgetItem);
    int deleteById(@Param("id") Long id);
}













