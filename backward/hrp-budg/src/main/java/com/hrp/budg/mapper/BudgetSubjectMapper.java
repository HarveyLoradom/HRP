package com.hrp.budg.mapper;

import com.hrp.common.entity.BudgetSubject;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 预算主体数据访问接口
 */
public interface BudgetSubjectMapper {
    BudgetSubject selectById(@Param("id") Long id);
    BudgetSubject selectByCode(@Param("code") String code);
    List<BudgetSubject> selectAll();
    List<BudgetSubject> selectByParentId(@Param("parentId") Long parentId);
    List<BudgetSubject> selectByDeptId(@Param("deptId") Long deptId);
    List<BudgetSubject> selectTree();
    int insert(BudgetSubject budgetSubject);
    int updateById(BudgetSubject budgetSubject);
    int deleteById(@Param("id") Long id);
}













