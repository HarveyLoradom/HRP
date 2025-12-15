package com.hrp.hr.service;

import java.util.List;
import java.util.Map;

public interface BusinessApplyService {
    List<?> getAll();
    List<?> getMyList(String userId, Long empId);
    Object getById(Long id);
    boolean save(Map<String, Object> data);
    boolean update(Map<String, Object> data);
    boolean delete(Long id);
    boolean submit(Long id);
    boolean withdraw(Long id);
    List<?> getRecords(Long id);
}





