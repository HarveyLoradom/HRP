package com.hrp.hr.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hrp.common.entity.HrAttRecord;
import com.hrp.common.entity.PageResult;
import com.hrp.common.exception.BusinessException;
import com.hrp.hr.mapper.HrAttRecordMapper;
import com.hrp.hr.service.HrAttRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class HrAttRecordServiceImpl implements HrAttRecordService {

    @Autowired
    private HrAttRecordMapper hrAttRecordMapper;
    
    // 正常上班时间：9:00
    private static final LocalTime WORK_START_TIME = LocalTime.of(9, 0);
    // 正常下班时间：18:00
    private static final LocalTime WORK_END_TIME = LocalTime.of(18, 0);

    @Override
    public HrAttRecord getById(Long recordId) {
        return hrAttRecordMapper.selectById(recordId);
    }

    @Override
    public PageResult<HrAttRecord> getPage(Long page, Long size, Long empId, 
                                           String attDate, String startDate, String endDate, String attStatus,
                                           String attType, String attSubType) {
        PageHelper.startPage(page.intValue(), size.intValue());
        List<HrAttRecord> list = hrAttRecordMapper.selectByConditions(empId, attDate, startDate, endDate, attStatus, attType, attSubType);
        PageInfo<HrAttRecord> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getList(), pageInfo.getTotal(), size, page);
    }

    @Override
    @Transactional
    public HrAttRecord save(HrAttRecord record) {
        if (record.getEmpId() == null) {
            throw new BusinessException("员工ID不能为空");
        }
        hrAttRecordMapper.insert(record);
        return record;
    }

    @Override
    @Transactional
    public HrAttRecord update(HrAttRecord record) {
        if (record.getRecordId() == null) {
            throw new BusinessException("记录ID不能为空");
        }
        hrAttRecordMapper.updateById(record);
        return hrAttRecordMapper.selectById(record.getRecordId());
    }

    @Override
    @Transactional
    public boolean delete(Long recordId) {
        return hrAttRecordMapper.deleteById(recordId) > 0;
    }

    @Override
    public HrAttRecord getTodayRecord(Long empId, String attDate) {
        return hrAttRecordMapper.selectTodayByEmpId(empId, attDate);
    }

    @Override
    @Transactional
    public HrAttRecord clockIn(Long empId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        String attDateStr = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        // 检查今天是否已经打过上班卡
        HrAttRecord todayRecord = hrAttRecordMapper.selectTodayByEmpId(empId, attDateStr);
        if (todayRecord != null && todayRecord.getAttStartTime() != null) {
            throw new BusinessException("今天已经打过上班卡了");
        }
        
        // 判断是否迟到（9点之后）
        LocalTime nowTime = now.toLocalTime();
        String attStatus = "NORMAL";
        if (nowTime.isAfter(WORK_START_TIME)) {
            attStatus = "LATE";
        }
        
        HrAttRecord record = new HrAttRecord();
        record.setEmpId(empId);
        record.setAttDate(today);
        record.setAttStartTime(now);
        record.setAttStatus(attStatus);
        record.setIsSupplement(0);
        record.setDay("0");
        
        hrAttRecordMapper.insert(record);
        return hrAttRecordMapper.selectById(record.getRecordId());
    }

    @Override
    @Transactional
    public HrAttRecord clockOut(Long empId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        String attDateStr = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        // 查询今天的打卡记录
        HrAttRecord todayRecord = hrAttRecordMapper.selectTodayByEmpId(empId, attDateStr);
        if (todayRecord == null || todayRecord.getAttStartTime() == null) {
            throw new BusinessException("请先打上班卡");
        }
        if (todayRecord.getAttEndTime() != null) {
            throw new BusinessException("今天已经打过下班卡了");
        }
        
        // 判断是否早退（18点之前）
        LocalTime nowTime = now.toLocalTime();
        String attStatus = todayRecord.getAttStatus();
        if (nowTime.isBefore(WORK_END_TIME)) {
            attStatus = "EARLY_LEAVE";
        }
        
        // 计算工时（小时）
        long hours = java.time.Duration.between(todayRecord.getAttStartTime(), now).toHours();
        // 计算天数：工时 >= 9小时为1，否则为0.5，如果工时不足则保持0
        String dayValue = "0";
        if (hours >= 9) {
            dayValue = "1";
        } else if (hours >= 4) {
            dayValue = "0.5";
        }
        
        // 更新下班时间和状态
        todayRecord.setAttEndTime(now);
        todayRecord.setAttStatus(attStatus);
        todayRecord.setDay(dayValue);
        
        hrAttRecordMapper.updateById(todayRecord);
        return hrAttRecordMapper.selectById(todayRecord.getRecordId());
    }
}
