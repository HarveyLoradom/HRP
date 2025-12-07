package com.hrp.efficiency.service;

import com.hrp.common.entity.IncomeEquipment;

import java.time.LocalDate;
import java.util.List;

public interface IncomeEquipmentService {
    List<IncomeEquipment> getAll();
    List<IncomeEquipment> getByEquipmentId(Long equipmentId);
    List<IncomeEquipment> getByDateRange(LocalDate startDate, LocalDate endDate);
    IncomeEquipment getById(Long id);
    boolean save(IncomeEquipment incomeEquipment);
    boolean update(IncomeEquipment incomeEquipment);
    boolean delete(Long id);
}

