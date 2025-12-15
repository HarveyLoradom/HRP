package com.hrp.efficiency.service;

import java.util.List;
import java.util.Map;

public interface InvestmentService {
    List<?> getAll();
    Object getById(Long id);
    boolean save(Map<String, Object> data);
    Map<String, Object> calculate(Map<String, Object> data);
}





