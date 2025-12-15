package com.hrp.hr.service.impl;

import com.hrp.hr.mapper.LeaveMapper;
import com.hrp.hr.service.LeaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LeaveServiceImpl implements LeaveService {

    @Autowired
    private LeaveMapper leaveMapper;

    @Override
    public List<?> getMyList(Long empId) {
        if (empId == null) {
            return List.of();
        }
        // 获取自己发起的请假申请
        return leaveMapper.selectByEmpId(empId);
    }

    @Override
    public Object getDetail(Long id) {
        return leaveMapper.selectById(id);
    }

    @Override
    public List<?> getRecords(Long id) {
        return leaveMapper.selectRecordsByLeaveId(id);
    }

    @Override
    @Transactional
    public boolean approve(Long id, String userId, String opinion) {
        Object leave = leaveMapper.selectById(id);
        if (leave == null) {
            return false;
        }
        
        // 保存审批记录
        Map<String, Object> record = new HashMap<>();
        record.put("leaveId", id);
        record.put("approverId", userId);
        record.put("approvalType", "APPROVE");
        record.put("approvalOpinion", opinion);
        record.put("approvalTime", LocalDateTime.now());
        leaveMapper.insertRecord(record);
        
        // 更新状态
        Map<String, Object> update = new HashMap<>();
        update.put("leaveId", id);
        update.put("status", "APPROVED");
        update.put("updateTime", LocalDateTime.now());
        return leaveMapper.updateById(update) > 0;
    }

    @Override
    @Transactional
    public boolean reject(Long id, String userId, String opinion) {
        Object leave = leaveMapper.selectById(id);
        if (leave == null) {
            return false;
        }
        
        // 保存审批记录
        Map<String, Object> record = new HashMap<>();
        record.put("leaveId", id);
        record.put("approverId", userId);
        record.put("approvalType", "REJECT");
        record.put("approvalOpinion", opinion);
        record.put("approvalTime", LocalDateTime.now());
        leaveMapper.insertRecord(record);
        
        // 更新状态
        Map<String, Object> update = new HashMap<>();
        update.put("leaveId", id);
        update.put("status", "REJECTED");
        update.put("updateTime", LocalDateTime.now());
        return leaveMapper.updateById(update) > 0;
    }
}





