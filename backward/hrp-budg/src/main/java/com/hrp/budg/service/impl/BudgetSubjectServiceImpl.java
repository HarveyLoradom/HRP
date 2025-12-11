package com.hrp.budg.service.impl;

import com.hrp.budg.mapper.BudgetSubjectMapper;
import com.hrp.budg.service.BudgetSubjectService;
import com.hrp.common.entity.BudgetSubject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BudgetSubjectServiceImpl implements BudgetSubjectService {

    @Autowired
    private BudgetSubjectMapper budgetSubjectMapper;

    @Override
    public BudgetSubject getById(Long id) {
        return budgetSubjectMapper.selectById(id);
    }

    @Override
    public BudgetSubject getByCode(String code) {
        return budgetSubjectMapper.selectByCode(code);
    }

    @Override
    public List<BudgetSubject> getAll() {
        return budgetSubjectMapper.selectAll();
    }

    @Override
    public List<BudgetSubject> getByParentId(Long parentId) {
        return budgetSubjectMapper.selectByParentId(parentId);
    }

    @Override
    public List<BudgetSubject> getByDeptId(Long deptId) {
        return budgetSubjectMapper.selectByDeptId(deptId);
    }

    @Override
    public List<BudgetSubject> getTree() {
        return budgetSubjectMapper.selectTree();
    }

    @Override
    @Transactional
    public boolean save(BudgetSubject budgetSubject) {
        if (budgetSubject.getSubjectLevel() == null) {
            budgetSubject.setSubjectLevel(1);
        }
        if (budgetSubject.getIsStop() == null) {
            budgetSubject.setIsStop(0L);
        }
        return budgetSubjectMapper.insert(budgetSubject) > 0;
    }

    @Override
    @Transactional
    public boolean update(BudgetSubject budgetSubject) {
        return budgetSubjectMapper.updateById(budgetSubject) > 0;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        return budgetSubjectMapper.deleteById(id) > 0;
    }
}




