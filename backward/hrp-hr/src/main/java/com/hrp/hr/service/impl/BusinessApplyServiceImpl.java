package com.hrp.hr.service.impl;

import com.hrp.hr.service.BusinessApplyService;
import com.hrp.hr.mapper.BusinessApplyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class BusinessApplyServiceImpl implements BusinessApplyService {

    @Autowired
    private BusinessApplyMapper businessApplyMapper;

    @Override
    public List<?> getAll() {
        return businessApplyMapper.selectAll();
    }

    @Override
    public List<?> getMyList(String userId, Long empId) {
        List<?> myCreated = empId != null ? businessApplyMapper.selectByApplicant(empId) : Collections.emptyList();
        List<?> myApproval = businessApplyMapper.selectByApprover(userId);
        
        Set<Long> idSet = new HashSet<>();
        List<Object> result = new ArrayList<>();
        
        if (myCreated != null) {
            for (Object item : myCreated) {
                Long id = getApplyId(item);
                if (id != null && !idSet.contains(id)) {
                    idSet.add(id);
                    result.add(item);
                }
            }
        }
        
        if (myApproval != null) {
            for (Object item : myApproval) {
                Long id = getApplyId(item);
                if (id != null && !idSet.contains(id)) {
                    idSet.add(id);
                    result.add(item);
                }
            }
        }
        
        return result;
    }

    @Override
    public Object getById(Long id) {
        return businessApplyMapper.selectById(id);
    }

    @Override
    @Transactional
    public boolean save(Map<String, Object> data) {
        if (data.get("applyId") == null) {
            // 新增
            data.put("applyNo", "HR-" + System.currentTimeMillis());
            if (data.get("status") == null) {
                data.put("status", "DRAFT");
            }
            data.put("createTime", LocalDateTime.now());
            return businessApplyMapper.insert(data) > 0;
        } else {
            // 更新
            data.put("updateTime", LocalDateTime.now());
            return businessApplyMapper.updateById(data) > 0;
        }
    }

    @Override
    @Transactional
    public boolean update(Map<String, Object> data) {
        return save(data);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        return businessApplyMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional
    public boolean submit(Long id) {
        Object apply = businessApplyMapper.selectById(id);
        if (apply == null) {
            return false;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("applyId", id);
        data.put("status", "PENDING");
        data.put("updateTime", LocalDateTime.now());
        return businessApplyMapper.updateById(data) > 0;
    }

    @Override
    @Transactional
    public boolean withdraw(Long id) {
        Object apply = businessApplyMapper.selectById(id);
        if (apply == null) {
            return false;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("applyId", id);
        data.put("status", "WITHDRAWN");
        data.put("updateTime", LocalDateTime.now());
        return businessApplyMapper.updateById(data) > 0;
    }

    @Override
    public List<?> getRecords(Long id) {
        return businessApplyMapper.selectRecordsByApplyId(id);
    }

    private Long getApplyId(Object item) {
        if (item instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) item;
            Object id = map.get("applyId");
            if (id instanceof Number) {
                return ((Number) id).longValue();
            }
        }
        return null;
    }
}





