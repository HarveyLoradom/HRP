package com.hrp.hr.service.impl;

import com.hrp.hr.mapper.AttendanceMapper;
import com.hrp.hr.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    @Autowired
    private AttendanceMapper attendanceMapper;

    @Override
    public List<?> getMyList(Long empId, String month) {
        if (empId == null) {
            return List.of();
        }
        return attendanceMapper.selectByEmpIdAndMonth(empId, month);
    }
}





