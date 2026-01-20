package com.hrp.budg.service;

import com.hrp.common.entity.BudgetDetailRecord;

import java.util.List;

/**
 * 预算明细记录服务接口
 */
public interface BudgetDetailRecordService {
    /**
     * 根据业务单号查询预算明细记录
     */
    List<BudgetDetailRecord> getByBusinessNo(String businessNo);
    
    /**
     * 保存预算明细记录（批量）
     */
    boolean saveBatch(List<BudgetDetailRecord> records);
    
    /**
     * 根据业务单号取消预算明细记录
     */
    boolean cancelByBusinessNo(String businessNo);
    
    /**
     * 根据业务ID取消预算明细记录
     */
    boolean cancelByBusinessId(Long businessId);
    
    /**
     * 批量更新预算明细记录（根据detailId更新）
     */
    boolean updateBatch(List<BudgetDetailRecord> records);
    
    /**
     * 根据subject_code和item_code查询申请单（detail_type='APPLY'）
     */
    List<BudgetDetailRecord> getAppliesBySubjectAndItem(String subjectCode, String itemCode);
}

