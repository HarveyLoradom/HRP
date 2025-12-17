package com.hrp.budg.service;

import com.hrp.common.entity.BudgetItem;
import com.hrp.common.entity.PageResult;

import java.util.List;

/**
 * 预算项目服务接口
 */
public interface BudgetItemService {
    BudgetItem getById(Long id);
    BudgetItem getByCode(String code);
    List<BudgetItem> getAll();
    List<BudgetItem> getByCategoryId(Long categoryId);
    /**
     * 分页查询项目预算
     */
    PageResult<BudgetItem> getPage(Long page, Long size, String budgetYear, String categoryType,
                                  Long level1CategoryId, Long level2CategoryId, String itemName);
    boolean save(BudgetItem budgetItem);
    boolean update(BudgetItem budgetItem);
    boolean delete(Long id);
    /**
     * 停用项目
     */
    boolean stop(Long id);
    /**
     * 启用项目
     */
    boolean start(Long id);
    /**
     * 分配主体
     */
    boolean assignSubjects(Long itemId, List<Long> subjectIds);
    /**
     * 获取项目分配的主体列表
     */
    List<com.hrp.common.entity.BudgetSubject> getAssignedSubjects(Long itemId);
}













