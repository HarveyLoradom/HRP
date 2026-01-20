package com.hrp.budg.service.impl;

import com.hrp.budg.mapper.BudgetDetailRecordMapper;
import com.hrp.budg.service.BudgetDetailRecordService;
import com.hrp.common.entity.BudgetDetailRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 预算明细记录服务实现
 */
@Service
public class BudgetDetailRecordServiceImpl implements BudgetDetailRecordService {
    
    @Autowired
    private BudgetDetailRecordMapper budgetDetailRecordMapper;
    
    @Override
    public List<BudgetDetailRecord> getByBusinessNo(String businessNo) {
        return budgetDetailRecordMapper.selectByBusinessNo(businessNo);
    }
    
    @Override
    @Transactional
    public boolean saveBatch(List<BudgetDetailRecord> records) {
        if (records == null || records.isEmpty()) {
            return true;
        }
        for (BudgetDetailRecord record : records) {
            try {
                // 验证必填字段
                if (record.getSubjectId() == null || record.getItemId() == null) {
                    throw new IllegalArgumentException("预算主体ID和预算项目ID不能为空");
                }
                if (record.getBusinessNo() == null || record.getBusinessNo().trim().isEmpty()) {
                    throw new IllegalArgumentException("业务单号不能为空");
                }
                if (record.getAmount() == null) {
                    throw new IllegalArgumentException("金额不能为空");
                }
                int result = budgetDetailRecordMapper.insert(record);
                if (result <= 0) {
                    throw new RuntimeException("插入预算明细记录失败");
                }
            } catch (Exception e) {
                System.err.println("保存预算明细记录失败: " + e.getMessage());
                System.err.println("记录信息: " + record);
                e.printStackTrace();
                throw new RuntimeException("保存预算明细记录失败: " + e.getMessage(), e);
            }
        }
        return true;
    }
    
    @Override
    @Transactional
    public boolean cancelByBusinessNo(String businessNo) {
        budgetDetailRecordMapper.cancelByBusinessNo(businessNo);
        return true;
    }
    
    @Override
    @Transactional
    public boolean cancelByBusinessId(Long businessId) {
        budgetDetailRecordMapper.cancelByBusinessId(businessId);
        return true;
    }
    
    @Override
    @Transactional
    public boolean updateBatch(List<BudgetDetailRecord> records) {
        if (records == null || records.isEmpty()) {
            return true;
        }
        for (BudgetDetailRecord record : records) {
            if (record.getDetailId() != null) {
                budgetDetailRecordMapper.updateById(record);
            }
        }
        return true;
    }
    
    @Override
    public List<BudgetDetailRecord> getAppliesBySubjectAndItem(String subjectCode, String itemCode) {
        return budgetDetailRecordMapper.selectAppliesBySubjectAndItem(subjectCode, itemCode);
    }
}

