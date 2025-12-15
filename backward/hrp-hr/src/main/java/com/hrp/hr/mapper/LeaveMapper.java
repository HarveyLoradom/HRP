package com.hrp.hr.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface LeaveMapper {
    List<?> selectByEmpId(@Param("empId") Long empId);
    Object selectById(@Param("id") Long id);
    List<?> selectRecordsByLeaveId(@Param("leaveId") Long leaveId);
    int insertRecord(Map<String, Object> record);
    int updateById(Map<String, Object> data);
}





