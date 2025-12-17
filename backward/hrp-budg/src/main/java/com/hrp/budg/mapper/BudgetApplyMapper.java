package com.hrp.budg.mapper;

import com.hrp.common.entity.BudgetApply;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 预算申请数据访问接口
 */
public interface BudgetApplyMapper {
    BudgetApply selectById(@Param("id") Long id);
    List<BudgetApply> selectPage(@Param("offset") long offset, @Param("size") long size);
    long countAll();
    int insert(BudgetApply budgetApply);
    int updateById(BudgetApply budgetApply);
    int deleteById(@Param("id") Long id);
}

