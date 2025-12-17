package com.hrp.budg.mapper;

import com.hrp.common.entity.BudgetItemSubject;
import com.hrp.common.entity.BudgetSubject;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 预算项目主体分配数据访问接口
 */
public interface BudgetItemSubjectMapper {
    /**
     * 根据项目ID查询分配的主体列表
     */
    List<BudgetSubject> selectSubjectsByItemId(@Param("itemId") Long itemId);
    
    /**
     * 根据项目ID删除所有分配关系
     */
    int deleteByItemId(@Param("itemId") Long itemId);
    
    /**
     * 插入分配关系
     */
    int insert(BudgetItemSubject itemSubject);
    
    /**
     * 批量插入分配关系
     */
    int insertBatch(@Param("list") List<BudgetItemSubject> list);
}

