package com.hrp.budg.mapper;

import com.hrp.common.entity.BudgetSubjectDept;
import com.hrp.common.entity.Dept;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 预算主体关联科室数据访问接口
 */
public interface BudgetSubjectDeptMapper {
    List<BudgetSubjectDept> selectBySubjectId(@Param("subjectId") Long subjectId);
    int insert(BudgetSubjectDept relation);
    int deleteBySubjectId(@Param("subjectId") Long subjectId);
    List<Dept> selectDeptsByIds(@Param("deptIds") List<Long> deptIds);
}

