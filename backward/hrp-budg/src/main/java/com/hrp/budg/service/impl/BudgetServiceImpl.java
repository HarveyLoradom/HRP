package com.hrp.budg.service.impl;

import com.hrp.budg.mapper.BudgetMapper;
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
        if (budget.getOccupiedAmount() == null) {
            budget.setOccupiedAmount(BigDecimal.ZERO);
        }
        if (budget.getExecutedAmount() == null) {
            budget.setExecutedAmount(BigDecimal.ZERO);
        }
        if (budget.getRemainingAmount() == null) {
            budget.setRemainingAmount(budget.getBudgetAmount());
        }
        if (budget.getExecutionRate() == null) {
            budget.setExecutionRate(BigDecimal.ZERO);
        }
        if (budget.getStatus() == null) {
            budget.setStatus("DRAFT");
        }
        return budgetMapper.insert(budget) > 0;
    }

    @Override
    @Transactional
    public boolean update(Budget budget) {
        // 重新计算剩余金额和执行率
        if (budget.getBudgetAmount() != null && budget.getExecutedAmount() != null && budget.getOccupiedAmount() != null) {
            budget.setRemainingAmount(budget.getBudgetAmount().subtract(budget.getOccupiedAmount()).subtract(budget.getExecutedAmount()));
            if (budget.getBudgetAmount().compareTo(BigDecimal.ZERO) > 0) {
                budget.setExecutionRate(budget.getExecutedAmount().divide(budget.getBudgetAmount(), 4, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal("100")));
            }
        }
        return budgetMapper.updateById(budget) > 0;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        return budgetMapper.deleteById(id) > 0;
    }

    @Override
    public boolean checkBudgetAmount(Long budgetId, BigDecimal amount) {
        Budget budget = budgetMapper.selectById(budgetId);
        if (budget == null) {
            return false;
        }
        BigDecimal available = budget.getRemainingAmount();
        return available != null && available.compareTo(amount) >= 0;
    }

    @Override
    @Transactional
    public boolean executeBudget(Long budgetId, BigDecimal amount) {
        Budget budget = budgetMapper.selectById(budgetId);
        if (budget == null) {
            return false;
        }
        // 更新执行金额
        budget.setExecutedAmount(budget.getExecutedAmount().add(amount));
        budget.setRemainingAmount(budget.getBudgetAmount().subtract(budget.getOccupiedAmount()).subtract(budget.getExecutedAmount()));
        if (budget.getBudgetAmount().compareTo(BigDecimal.ZERO) > 0) {
            budget.setExecutionRate(budget.getExecutedAmount().divide(budget.getBudgetAmount(), 4, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal("100")));
        }
        return budgetMapper.updateById(budget) > 0;
    }
}

