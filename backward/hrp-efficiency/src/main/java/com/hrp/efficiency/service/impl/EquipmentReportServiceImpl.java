package com.hrp.efficiency.service.impl;

import com.hrp.efficiency.service.EquipmentReportService;
import com.hrp.efficiency.mapper.EquipmentReportMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EquipmentReportServiceImpl implements EquipmentReportService {

    @Autowired
    private EquipmentReportMapper equipmentReportMapper;

    @Override
    public List<?> getAll() {
        return equipmentReportMapper.selectAll();
    }

    @Override
    public Object getById(Long id) {
        return equipmentReportMapper.selectById(id);
    }

    @Override
    @Transactional
    public Object generate(Map<String, Object> data) {
        String reportNo = "EQ-" + System.currentTimeMillis();
        data.put("reportNo", reportNo);
        data.put("createTime", LocalDateTime.now());
        
        if (equipmentReportMapper.insert(data) > 0) {
            return equipmentReportMapper.selectByReportNo(reportNo);
        }
        return null;
    }
}





