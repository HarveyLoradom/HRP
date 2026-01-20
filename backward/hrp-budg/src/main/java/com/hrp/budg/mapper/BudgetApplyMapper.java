package com.hrp.budg.mapper;

import com.hrp.common.entity.BudgetApply;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 预算申请数据访问接口
 */
public interface BudgetApplyMapper {
    BudgetApply selectById(@Param("id") Long id);
    BudgetApply selectByNo(@Param("applyNo") String applyNo);
    List<BudgetApply> selectAll();
    List<BudgetApply> selectPage(@Param("offset") long offset, @Param("size") long size);
    List<BudgetApply> selectByItemId(@Param("itemId") Long itemId);
    List<BudgetApply> selectByConditions(@Param("applyNo") String applyNo, 
                                         @Param("itemId") Long itemId, 
                                         @Param("applicantName") String applicantName,
                                         @Param("applicantCode") String applicantCode,
                                         @Param("status") String status,
                                         @Param("startDate") String startDate,
                                         @Param("endDate") String endDate);
    long countAll();
    String selectMaxApplyNoByPrefix(@Param("prefix") String prefix);
    /**
     * 检查是否存在相同的申请（同一项目+同一主体+同一年度）
     * @param itemId 预算项目ID
     * @param subjectId 预算主体ID
     * @param budgetYear 预算年度
     * @param excludeApplyId 排除的申请ID（用于更新时排除自己）
     * @return 存在的申请记录
     */
    BudgetApply selectByItemSubjectYear(@Param("itemId") Long itemId, 
                                        @Param("subjectId") Long subjectId, 
                                        @Param("budgetYear") String budgetYear,
                                        @Param("excludeApplyId") Long excludeApplyId);
    int insert(BudgetApply budgetApply);
    int updateById(BudgetApply budgetApply);
    int deleteById(@Param("id") Long id);
}

