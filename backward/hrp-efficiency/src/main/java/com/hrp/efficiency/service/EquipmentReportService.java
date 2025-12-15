package com.hrp.efficiency.service;

import java.util.List;
import java.util.Map;

public interface EquipmentReportService {
    List<?> getAll();
    Object getById(Long id);
    Object generate(Map<String, Object> data);
}





