package com.hrp.hr.mapper;

import com.hrp.common.entity.HrAttRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 员工考勤记录表数据访问接口
 */
public interface HrAttRecordMapper {
    HrAttRecord selectById(@Param("recordId") Long recordId);
    List<HrAttRecord> selectByConditions(@Param("empId") Long empId,
                                         @Param("attDate") String attDate,
                                         @Param("startDate") String startDate,
                                         @Param("endDate") String endDate,
                                         @Param("attStatus") String attStatus,
                                         @Param("attType") String attType,
                                         @Param("attSubType") String attSubType);
    HrAttRecord selectTodayByEmpId(@Param("empId") Long empId, @Param("attDate") String attDate);

    /**
     * 按周期汇总（按员工分组）：正常出勤/请假/加班天数
     * Map keys: empId, normalDays, leaveDays, overtimeDays
     */
    List<Map<String, Object>> selectSummaryByRange(@Param("startDate") String startDate, @Param("endDate") String endDate);
    int insert(HrAttRecord record);
    int updateById(HrAttRecord record);
    int deleteById(@Param("recordId") Long recordId);
}

