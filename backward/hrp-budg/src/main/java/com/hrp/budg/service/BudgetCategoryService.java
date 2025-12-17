package com.hrp.budg.service;

import com.hrp.common.entity.BudgetCategory;
import com.hrp.common.entity.PageResult;

import java.util.List;

/**
 * 预算分类服务接口
 */
public interface BudgetCategoryService {
    PageResult<BudgetCategory> getLevel1Page(Long page, Long size, String budgetYear, String categoryType);
    PageResult<BudgetCategory> getLevel2Page(Long page, Long size, String budgetYear, String categoryType, Long parentCategoryId);
    List<BudgetCategory> getLevel1List(String budgetYear, String categoryType);
    List<BudgetCategory> getLevel2List(String budgetYear, String categoryType, Long parentCategoryId);
    BudgetCategory getById(Long id);
    boolean save(BudgetCategory category);
    boolean update(BudgetCategory category);
    boolean delete(Long id);
    boolean stop(Long id);
    boolean start(Long id);
    /**
     * 生成一级分类编码
     * 规则：年度+预算分类（大写首字母）+01+001
     * 例如：2025SR01（2025年收入预算第一个一级分类）
     */
    String generateLevel1Code(String budgetYear, String categoryType);
    
    /**
     * 生成二级分类编码
     * 规则：在上级分类编码后追加3位序号
     * 例如：2025SR01001（2025年收入预算第一个一级分类下的第一个二级分类）
     */
    String generateLevel2Code(String parentCategoryCode);
}

