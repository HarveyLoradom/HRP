package com.hrp.budg.service.impl;

import com.hrp.budg.mapper.BudgetApplyMapper;
import com.hrp.budg.service.BudgetApplyService;
import com.hrp.common.entity.BudgetApply;
import com.hrp.common.entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 预算申请服务实现
 */
@Service
public class BudgetApplyServiceImpl implements BudgetApplyService {

    @Autowired
    private BudgetApplyMapper budgetApplyMapper;

    @Override
    public BudgetApply getById(Long id) {
        return budgetApplyMapper.selectById(id);
    }

    @Override
    public PageResult<BudgetApply> getPage(Long page, Long size) {
        long offset = (page - 1) * size;
        long total = budgetApplyMapper.countAll();
        List<BudgetApply> records = budgetApplyMapper.selectPage(offset, size);
        return new PageResult<>(records, total, page, size);
    }

    @Override
    @Transactional
    public boolean save(BudgetApply budgetApply) {
        if (budgetApply.getApplyNo() == null || budgetApply.getApplyNo().isEmpty()) {
            budgetApply.setApplyNo("BUDGET_APPLY_" + System.currentTimeMillis());
        }
        if (budgetApply.getStatus() == null) {
            budgetApply.setStatus("DRAFT");
        }
        budgetApply.setCreateTime(LocalDateTime.now());
        budgetApply.setUpdateTime(LocalDateTime.now());
        return budgetApplyMapper.insert(budgetApply) > 0;
    }

    @Override
    @Transactional
    public boolean update(BudgetApply budgetApply) {
        budgetApply.setUpdateTime(LocalDateTime.now());
        return budgetApplyMapper.updateById(budgetApply) > 0;
    }

    @Override
    @Transactional
    public boolean submit(Long id) {
        BudgetApply apply = budgetApplyMapper.selectById(id);
        if (apply == null) {
            return false;
        }
        apply.setStatus("PENDING");
        apply.setUpdateTime(LocalDateTime.now());
        // TODO: 启动流程
        return budgetApplyMapper.updateById(apply) > 0;
    }

    @Override
    @Transactional
    public boolean approve(Long id, String opinion) {
        BudgetApply apply = budgetApplyMapper.selectById(id);
        if (apply == null) {
            return false;
        }
        apply.setStatus("APPROVED");
        apply.setUpdateTime(LocalDateTime.now());
        // TODO: 完成流程审批
        return budgetApplyMapper.updateById(apply) > 0;
    }

    @Override
    @Transactional
    public boolean reject(Long id, String opinion) {
        BudgetApply apply = budgetApplyMapper.selectById(id);
        if (apply == null) {
            return false;
        }
        apply.setStatus("REJECTED");
        apply.setUpdateTime(LocalDateTime.now());
        // TODO: 完成流程拒绝
        return budgetApplyMapper.updateById(apply) > 0;
    }
}

