package com.hrp.hr.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface BusinessApplyMapper {
    List<?> selectAll();
    List<?> selectByApplicant(@Param("applicantId") Long applicantId);
    List<?> selectByApprover(@Param("approverId") String approverId);
    Object selectById(@Param("id") Long id);
    int insert(Map<String, Object> data);
    int updateById(Map<String, Object> data);
    int deleteById(@Param("id") Long id);
    List<?> selectRecordsByApplyId(@Param("applyId") Long applyId);
}





