package com.hrp.budg.mapper;

import com.hrp.common.entity.BudgetDetailRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 预算明细记录数据访问接口
 */
public interface BudgetDetailRecordMapper {
    BudgetDetailRecord selectById(@Param("id") Long id);
    List<BudgetDetailRecord> selectByBusinessNo(@Param("businessNo") String businessNo);
    /**
     * 根据subject_code、item_code查询申请单（detail_type='APPLY'）
     * @param subjectCode 预算主体编码
     * @param itemCode 预算项目编码
     * @return 申请单明细记录列表
     */
    List<BudgetDetailRecord> selectAppliesBySubjectAndItem(@Param("subjectCode") String subjectCode, @Param("itemCode") String itemCode);
    int insert(BudgetDetailRecord record);
    int updateById(BudgetDetailRecord record);
    int cancelByBusinessNo(@Param("businessNo") String businessNo);
    int cancelByBusinessId(@Param("businessId") Long businessId);
    /**
     * 根据业务单号、主体ID、项目ID和金额查询记录（用于查找申请冲销产生的负数记录）
     */
    List<BudgetDetailRecord> selectByBusinessNoAndSubjectItemAndAmount(
        @Param("businessNo") String businessNo,
        @Param("subjectId") Long subjectId,
        @Param("itemId") Long itemId,
        @Param("amount") java.math.BigDecimal amount
    );
    /**
     * 根据ID删除记录
     */
    int deleteById(@Param("id") Long id);
}

