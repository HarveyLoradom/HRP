package com.hrp.efficiency.service;

import com.hrp.common.entity.Equipment;

import java.util.List;

public interface EquipmentService {
    List<Equipment> getAll();
    List<Equipment> getByStatus(String status);
    Equipment getById(Long id);
    boolean save(Equipment equipment);
    boolean update(Equipment equipment);
    boolean delete(Long id);
}

