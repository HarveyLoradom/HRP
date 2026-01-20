package com.hrp.budg.service.impl;

import com.hrp.budg.mapper.BudgetDetailMapper;
import com.hrp.budg.service.BudgetDetailService;
import com.hrp.common.entity.BudgetDetail;
import com.hrp.common.entity.BudgetExecutionDetail;
import com.hrp.common.entity.BudgetApplyDetail;
import com.hrp.common.entity.PageResult;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
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
    public PageResult<BudgetDetail> getPage(Long page, Long size, Long subjectId, String budgetYear, String categoryType, String itemName) {
        Map<String, Object> params = new HashMap<>();
        params.put("subjectId", subjectId);
        params.put("budgetYear", budgetYear);
        params.put("categoryType", categoryType);
        params.put("itemName", itemName);
        
        PageHelper.startPage(page.intValue(), size.intValue());
        List<BudgetDetail> list = budgetDetailMapper.selectByParams(params);
        PageInfo<BudgetDetail> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), size, page);
    }

    @Override
    public List<BudgetExecutionDetail> getExecutionDetails(Long itemId, Long subjectId) {
        return budgetDetailMapper.selectExecutionDetails(itemId, subjectId);
    }

    @Override
    public List<BudgetApplyDetail> getApplyDetails(Long itemId, Long subjectId) {
        return budgetDetailMapper.selectApplyDetails(itemId, subjectId);
    }

    @Override
    public List<BudgetExecutionDetail> getApplyExecutionDetails(Long itemId, Long subjectId, String applyNo) {
        return budgetDetailMapper.selectApplyExecutionDetails(itemId, subjectId, applyNo);
    }
}

