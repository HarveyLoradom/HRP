package com.hrp.budg.service.impl;

import com.hrp.budg.mapper.BudgetMapper;
import com.hrp.budg.mapper.BudgetDetailRecordMapper;
import com.hrp.budg.service.BudgetService;
import com.hrp.common.entity.Budget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BudgetServiceImpl implements BudgetService {

    @Autowired
    private BudgetMapper budgetMapper;
    
    @Autowired
    private BudgetDetailRecordMapper budgetDetailRecordMapper;

    @Override
    public Budget getById(Long id) {
        return budgetMapper.selectById(id);
    }

    @Override
    public Budget getByNo(String no) {
        return budgetMapper.selectByNo(no);
    }

    @Override
    public List<Budget> getAll() {
        return budgetMapper.selectAll();
    }

    @Override
    public List<Budget> getByYear(String year) {
        return budgetMapper.selectByYear(year);
    }

    @Override
    public List<Budget> getBySubjectId(Long subjectId) {
        return budgetMapper.selectBySubjectId(subjectId);
    }

    @Override
    public List<Budget> getByItemId(Long itemId) {
        return budgetMapper.selectByItemId(itemId);
    }

    @Override
    public List<Budget> getBySubjectAndItem(Long subjectId, Long itemId) {
        return budgetMapper.selectBySubjectAndItem(subjectId, itemId);
    }

    @Override
    @Transactional
    public boolean save(Budget budget) {
        if (budget.getBudgetPeriod() == null) {
            budget.setBudgetPeriod("YEAR");
        }
        return budgetMapper.insert(budget) > 0;
    }

    @Override
    @Transactional
    public boolean update(Budget budget) {
        return budgetMapper.updateById(budget) > 0;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        return budgetMapper.deleteById(id) > 0;
    }

    @Override
    public boolean checkBudgetAmount(Long budgetId, BigDecimal amount) {
        // 现在金额通过 budget_detail 表汇总计算，此方法需要从明细表汇总计算剩余金额
        // TODO: 如果需要实时校验，可以从 budget_detail 表汇总计算剩余金额
        Budget budget = budgetMapper.selectById(budgetId);
        return budget != null && budget.getBudgetAmount() != null && budget.getBudgetAmount().compareTo(amount) >= 0;
    }

    @Override
    @Transactional
    public boolean executeBudget(Long budgetId, BigDecimal amount) {
        // 此方法已不再使用，执行金额通过 budget_detail 表记录
        // 保留方法签名以保持接口兼容性，但不执行任何操作
        // 实际应该在创建预算执行记录时，同时向 budget_detail 表插入明细记录
        return true;
    }
    
    @Override
    public BigDecimal getRemainingAmount(Long budgetId) {
        BigDecimal remaining = budgetMapper.getRemainingAmount(budgetId);
        return remaining != null ? remaining : BigDecimal.ZERO;
    }
}

