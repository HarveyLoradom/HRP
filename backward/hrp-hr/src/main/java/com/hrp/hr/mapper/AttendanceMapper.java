package com.hrp.hr.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AttendanceMapper {
    List<?> selectByEmpIdAndMonth(@Param("empId") Long empId, @Param("month") String month);
}





