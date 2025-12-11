package com.hrp.budg.mapper;

import com.hrp.common.entity.Budget;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 预算数据访问接口
 */
public interface BudgetMapper {
    Budget selectById(@Param("id") Long id);
    Budget selectByNo(@Param("no") String no);
    List<Budget> selectAll();
    List<Budget> selectByYear(@Param("year") String year);
    List<Budget> selectBySubjectId(@Param("subjectId") Long subjectId);
    List<Budget> selectByItemId(@Param("itemId") Long itemId);
    List<Budget> selectBySubjectAndItem(@Param("subjectId") Long subjectId, @Param("itemId") Long itemId);
    int insert(Budget budget);
    int updateById(Budget budget);
    int deleteById(@Param("id") Long id);
    int updateExecutionAmount(@Param("id") Long id, @Param("amount") java.math.BigDecimal amount);
}




