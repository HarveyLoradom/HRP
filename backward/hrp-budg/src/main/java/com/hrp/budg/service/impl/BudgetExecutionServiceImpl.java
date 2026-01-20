package com.hrp.budg.service.impl;

import com.hrp.budg.mapper.BudgetDetailRecordMapper;
import com.hrp.budg.mapper.BudgetExecutionMapper;
import com.hrp.budg.service.BudgetExecutionService;
import com.hrp.budg.service.BudgetService;
import com.hrp.common.entity.Budget;
import com.hrp.common.entity.BudgetDetailRecord;
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
    
    @Autowired
    private BudgetDetailRecordMapper budgetDetailRecordMapper;

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
        // businessNo 应该在调用时传入，如果没有则使用 executionNo
        // TODO: 需要调用方传入 businessNo（如 payout_billcode）
        String businessNo = execution.getBusinessNo();
        
        // 科室和人员信息需要根据业务类型获取
        // TODO: 如果是 PAYOUT 类型，需要从报账单获取 dept_id, emp_id 等信息
        // 由于跨模块依赖问题，这里暂时留空，实际使用时需要通过 RPC 调用或其他方式获取
        Long deptId = null;
        String deptCode = null;
        String deptName = null;
        Long empId = null;
        String empCode = null;
        String empName = null;
        
        boolean success = budgetExecutionMapper.insert(execution) > 0;
        if (success) {
            // 创建预算明细记录（执行）
            BudgetDetailRecord detailRecord = new BudgetDetailRecord();
            detailRecord.setBudgetYear(budget.getBudgetYear());
            detailRecord.setSubjectId(budget.getSubjectId());
            detailRecord.setSubjectCode(budget.getSubjectCode());
            detailRecord.setSubjectName(budget.getSubjectName());
            detailRecord.setItemId(budget.getItemId());
            detailRecord.setItemCode(budget.getItemCode());
            detailRecord.setItemName(budget.getItemName());
            detailRecord.setDetailType("PAYOUT"); // 预算执行记录为报账单类型
            detailRecord.setAmount(amount);
            detailRecord.setBusinessId(businessId);
            detailRecord.setBusinessNo(businessNo != null ? businessNo : execution.getExecutionNo());
            detailRecord.setDeptId(deptId);
            detailRecord.setDeptCode(deptCode);
            detailRecord.setDeptName(deptName);
            detailRecord.setEmpId(empId);
            detailRecord.setEmpCode(empCode);
            detailRecord.setEmpName(empName);
            detailRecord.setRemark("预算执行记录");
            detailRecord.setCreateUser(execution.getCreateUser() != null ? execution.getCreateUser() : "SYSTEM");
            
            budgetDetailRecordMapper.insert(detailRecord);
        }
        return success;
    }
}













