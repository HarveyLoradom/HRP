package com.hrp.budg.mapper;

import com.hrp.common.entity.BudgetDetail;
import com.hrp.common.entity.BudgetExecutionDetail;
import com.hrp.common.entity.BudgetApplyDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 预算明细数据访问接口
 */
public interface BudgetDetailMapper {
    List<BudgetDetail> selectPage(@Param("offset") long offset, @Param("size") long size, @Param("params") Map<String, Object> params);
    long countByParams(@Param("params") Map<String, Object> params);
    List<BudgetExecutionDetail> selectExecutionDetails(@Param("itemId") Long itemId, @Param("subjectId") Long subjectId);
    List<BudgetApplyDetail> selectApplyDetails(@Param("itemId") Long itemId, @Param("subjectId") Long subjectId);
}

