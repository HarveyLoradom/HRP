package com.hrp.efficiency.service.impl;

import com.hrp.efficiency.service.DataCollectionService;
import com.hrp.efficiency.mapper.DataCollectionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DataCollectionServiceImpl implements DataCollectionService {

    @Autowired
    private DataCollectionMapper dataCollectionMapper;

    @Override
    @Transactional
    public boolean collectDeptData(Map<String, Object> data) {
        data.put("collectionType", "DEPT");
        data.put("collectionTime", LocalDateTime.now());
        return dataCollectionMapper.insert(data) > 0;
    }

    @Override
    @Transactional
    public boolean collectEquipmentData(Map<String, Object> data) {
        data.put("collectionType", "EQUIPMENT");
        data.put("collectionTime", LocalDateTime.now());
        return dataCollectionMapper.insert(data) > 0;
    }

    @Override
    public List<?> getCollectionRecords(String collectionType) {
        return dataCollectionMapper.selectByType(collectionType);
    }
}





