package com.hrp.budg.service.impl;

import com.hrp.budg.mapper.BudgetExecutionMapper;
import com.hrp.budg.service.BudgetExecutionService;
import com.hrp.budg.service.BudgetService;
import com.hrp.common.entity.Budget;
import com.hrp.common.entity.BudgetExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BudgetExecutionServiceImpl implements BudgetExecutionService {

    @Autowired
    private BudgetExecutionMapper budgetExecutionMapper;

    @Autowired
    private BudgetService budgetService;

    @Override
    public BudgetExecution getById(Long id) {
        return budgetExecutionMapper.selectById(id);
    }

    @Override
    public BudgetExecution getByNo(String no) {
        return budgetExecutionMapper.selectByNo(no);
    }

    @Override
    public List<BudgetExecution> getAll() {
        return budgetExecutionMapper.selectAll();
    }

    @Override
    public List<BudgetExecution> getByBudgetId(Long budgetId) {
        return budgetExecutionMapper.selectByBudgetId(budgetId);
    }

    @Override
    public List<BudgetExecution> getByBusinessId(String businessType, Long businessId) {
        return budgetExecutionMapper.selectByBusinessId(businessType, businessId);
    }

    @Override
    @Transactional
    public boolean save(BudgetExecution budgetExecution) {
        if (budgetExecution.getExecutionNo() == null || budgetExecution.getExecutionNo().isEmpty()) {
            budgetExecution.setExecutionNo("EXEC" + System.currentTimeMillis());
        }
        if (budgetExecution.getExecutionDate() == null) {
            budgetExecution.setExecutionDate(LocalDateTime.now());
        }
        if (budgetExecution.getExecutionType() == null) {
            budgetExecution.setExecutionType("MANUAL");
        }
        if (budgetExecution.getStatus() == null) {
            budgetExecution.setStatus("PENDING");
        }
        return budgetExecutionMapper.insert(budgetExecution) > 0;
    }

    @Override
    @Transactional
    public boolean update(BudgetExecution budgetExecution) {
        return budgetExecutionMapper.updateById(budgetExecution) > 0;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        return budgetExecutionMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional
    public boolean createExecutionFromBusiness(String businessType, Long businessId, Long budgetId, java.math.BigDecimal amount) {
        Budget budget = budgetService.getById(budgetId);
        if (budget == null) {
            return false;
        }
        
        BudgetExecution execution = new BudgetExecution();
        execution.setExecutionNo("EXEC" + System.currentTimeMillis());
        execution.setBudgetId(budgetId);
        execution.setBudgetNo(budget.getBudgetNo());
        execution.setSubjectId(budget.getSubjectId());
        execution.setItemId(budget.getItemId());
        execution.setExecutionType("AUTO");
        execution.setExecutionAmount(amount);
        execution.setExecutionDate(LocalDateTime.now());
        execution.setBusinessType(businessType);
        execution.setBusinessId(businessId);
        execution.setStatus("APPROVED");
        
        boolean success = budgetExecutionMapper.insert(execution) > 0;
        if (success) {
            // 更新预算执行金额
            budgetService.executeBudget(budgetId, amount);
        }
        return success;
    }
}




