package com.hrp.efficiency.service;

import java.util.List;
import java.util.Map;

public interface DataCollectionService {
    boolean collectDeptData(Map<String, Object> data);
    boolean collectEquipmentData(Map<String, Object> data);
    List<?> getCollectionRecords(String collectionType);
}





