package com.hrp.budg.service.impl;

import com.hrp.budg.mapper.BudgetItemMapper;
import com.hrp.budg.service.BudgetItemService;
import com.hrp.common.entity.BudgetItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BudgetItemServiceImpl implements BudgetItemService {

    @Autowired
    private BudgetItemMapper budgetItemMapper;

    @Override
    public BudgetItem getById(Long id) {
        return budgetItemMapper.selectById(id);
    }

    @Override
    public BudgetItem getByCode(String code) {
        return budgetItemMapper.selectByCode(code);
    }

    @Override
    public List<BudgetItem> getAll() {
        return budgetItemMapper.selectAll();
    }

    @Override
    public List<BudgetItem> getByCategoryId(Long categoryId) {
        return budgetItemMapper.selectByCategoryId(categoryId);
    }

    @Override
    @Transactional
    public boolean save(BudgetItem budgetItem) {
        if (budgetItem.getIsCentralized() == null) {
            budgetItem.setIsCentralized(0L);
        }
        if (budgetItem.getAllowAdjust() == null) {
            budgetItem.setAllowAdjust(1L);
        }
        if (budgetItem.getIsStop() == null) {
            budgetItem.setIsStop(0L);
        }
        return budgetItemMapper.insert(budgetItem) > 0;
    }

    @Override
    @Transactional
    public boolean update(BudgetItem budgetItem) {
        return budgetItemMapper.updateById(budgetItem) > 0;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        return budgetItemMapper.deleteById(id) > 0;
    }
}




