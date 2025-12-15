package com.hrp.hr.service.impl;

import com.hrp.hr.mapper.TransferMapper;
import com.hrp.hr.service.TransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TransferServiceImpl implements TransferService {

    @Autowired
    private TransferMapper transferMapper;

    @Override
    public List<?> getMyList(Long empId) {
        if (empId == null) {
            return List.of();
        }
        return transferMapper.selectByEmpId(empId);
    }

    @Override
    public Object getDetail(Long id) {
        return transferMapper.selectById(id);
    }

    @Override
    public List<?> getRecords(Long id) {
        return transferMapper.selectRecordsByTransferId(id);
    }

    @Override
    @Transactional
    public boolean approve(Long id, String userId, String opinion) {
        Object transfer = transferMapper.selectById(id);
        if (transfer == null) {
            return false;
        }
        
        // 保存审批记录
        Map<String, Object> record = new HashMap<>();
        record.put("transferId", id);
        record.put("approverId", userId);
        record.put("approvalType", "APPROVE");
        record.put("approvalOpinion", opinion);
        record.put("approvalTime", LocalDateTime.now());
        transferMapper.insertRecord(record);
        
        // 更新状态
        Map<String, Object> update = new HashMap<>();
        update.put("transferId", id);
        update.put("status", "APPROVED");
        update.put("updateTime", LocalDateTime.now());
        return transferMapper.updateById(update) > 0;
    }

    @Override
    @Transactional
    public boolean reject(Long id, String userId, String opinion) {
        Object transfer = transferMapper.selectById(id);
        if (transfer == null) {
            return false;
        }
        
        // 保存审批记录
        Map<String, Object> record = new HashMap<>();
        record.put("transferId", id);
        record.put("approverId", userId);
        record.put("approvalType", "REJECT");
        record.put("approvalOpinion", opinion);
        record.put("approvalTime", LocalDateTime.now());
        transferMapper.insertRecord(record);
        
        // 更新状态
        Map<String, Object> update = new HashMap<>();
        update.put("transferId", id);
        update.put("status", "REJECTED");
        update.put("updateTime", LocalDateTime.now());
        return transferMapper.updateById(update) > 0;
    }
}





