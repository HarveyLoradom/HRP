package com.hrp.budg.service.impl;

import com.hrp.budg.mapper.BudgetDetailMapper;
import com.hrp.budg.service.BudgetDetailService;
import com.hrp.common.entity.BudgetDetail;
import com.hrp.common.entity.BudgetExecutionDetail;
import com.hrp.common.entity.BudgetApplyDetail;
import com.hrp.common.entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 预算明细服务实现
 */
@Service
public class BudgetDetailServiceImpl implements BudgetDetailService {

    @Autowired
    private BudgetDetailMapper budgetDetailMapper;

    @Override
    public PageResult<BudgetDetail> getPage(Long page, Long size, Long deptId, String budgetYear, String categoryType, String itemName) {
        long offset = (page - 1) * size;
        Map<String, Object> params = new HashMap<>();
        params.put("deptId", deptId);
        params.put("budgetYear", budgetYear);
        params.put("categoryType", categoryType);
        params.put("itemName", itemName);
        
        long total = budgetDetailMapper.countByParams(params);
        List<BudgetDetail> records = budgetDetailMapper.selectPage(offset, size, params);
        return new PageResult<>(records, total, page, size);
    }

    @Override
    public List<BudgetExecutionDetail> getExecutionDetails(Long itemId, Long subjectId) {
        return budgetDetailMapper.selectExecutionDetails(itemId, subjectId);
    }

    @Override
    public List<BudgetApplyDetail> getApplyDetails(Long itemId, Long subjectId) {
        return budgetDetailMapper.selectApplyDetails(itemId, subjectId);
    }
}

