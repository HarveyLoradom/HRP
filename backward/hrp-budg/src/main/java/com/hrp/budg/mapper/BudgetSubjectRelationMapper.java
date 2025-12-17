package com.hrp.budg.mapper;

import com.hrp.common.entity.BudgetSubjectRelation;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 预算主体关联数据访问接口
 */
public interface BudgetSubjectRelationMapper {
    List<BudgetSubjectRelation> selectBySubjectId(@Param("subjectId") Long subjectId);
    int insert(BudgetSubjectRelation relation);
    int deleteBySubjectId(@Param("subjectId") Long subjectId);
}

