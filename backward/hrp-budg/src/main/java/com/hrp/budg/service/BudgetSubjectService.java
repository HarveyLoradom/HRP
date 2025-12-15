package com.hrp.budg.service;

import com.hrp.common.entity.BudgetSubject;

import java.util.List;

/**
 * 预算主体服务接口
 */
public interface BudgetSubjectService {
    BudgetSubject getById(Long id);
    BudgetSubject getByCode(String code);
    List<BudgetSubject> getAll();
    List<BudgetSubject> getByParentId(Long parentId);
    List<BudgetSubject> getByDeptId(Long deptId);
    List<BudgetSubject> getTree();
    boolean save(BudgetSubject budgetSubject);
    boolean update(BudgetSubject budgetSubject);
    boolean delete(Long id);
}








