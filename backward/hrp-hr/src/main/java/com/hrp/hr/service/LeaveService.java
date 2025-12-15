package com.hrp.hr.service;

import java.util.List;
import java.util.Map;

public interface LeaveService {
    List<?> getMyList(Long empId);
    Object getDetail(Long id);
    List<?> getRecords(Long id);
    boolean approve(Long id, String userId, String opinion);
    boolean reject(Long id, String userId, String opinion);
}





