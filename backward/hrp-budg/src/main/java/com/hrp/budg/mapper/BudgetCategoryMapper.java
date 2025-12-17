package com.hrp.budg.mapper;

import com.hrp.common.entity.BudgetCategory;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 预算分类数据访问接口
 */
public interface BudgetCategoryMapper {
    List<BudgetCategory> selectLevel1ByConditions(@Param("budgetYear") String budgetYear, 
                                                   @Param("categoryType") String categoryType);
    List<BudgetCategory> selectLevel2ByConditions(@Param("budgetYear") String budgetYear,
                                                   @Param("categoryType") String categoryType,
                                                   @Param("parentCategoryId") Long parentCategoryId);
    BudgetCategory selectById(@Param("id") Long id);
    BudgetCategory selectByCode(@Param("code") String code);
    int insert(BudgetCategory category);
    int updateById(BudgetCategory category);
    int deleteById(@Param("id") Long id);
    /**
     * 检查一级分类下是否有二级分类
     */
    int countLevel2ByParentId(@Param("parentCategoryId") Long parentCategoryId);
    /**
     * 检查二级分类下是否有项目预算
     */
    int countItemsByCategoryId(@Param("categoryId") Long categoryId);
    /**
     * 查询指定年度和分类类型的最大一级分类序号
     */
    String selectMaxLevel1Code(@Param("budgetYear") String budgetYear, @Param("categoryType") String categoryType);
    /**
     * 查询指定父分类下的最大二级分类序号
     */
    String selectMaxLevel2Code(@Param("parentCategoryCode") String parentCategoryCode);
}

