package com.hrp.hr.service;

import com.hrp.common.entity.HrAttRecord;
import java.util.List;

/**
 * 员工考勤记录服务接口
 */
public interface HrAttRecordService {
    /**
     * 根据ID查询
     */
    HrAttRecord getById(Long recordId);
    
    /**
     * 分页查询
     */
    com.hrp.common.entity.PageResult<HrAttRecord> getPage(Long page, Long size, Long empId, 
                                                          String attDate, String startDate, String endDate, String attStatus,
                                                          String attType, String attSubType);
    
    /**
     * 保存考勤记录
     */
    HrAttRecord save(HrAttRecord record);
    
    /**
     * 更新考勤记录
     */
    HrAttRecord update(HrAttRecord record);
    
    /**
     * 删除考勤记录
     */
    boolean delete(Long recordId);
    
    /**
     * 查询今日打卡记录
     */
    HrAttRecord getTodayRecord(Long empId, String attDate);
    
    /**
     * 上班打卡
     */
    HrAttRecord clockIn(Long empId);
    
    /**
     * 下班打卡
     */
    HrAttRecord clockOut(Long empId);
}
