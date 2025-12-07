package com.hrp.efficiency.service.impl;

import com.hrp.efficiency.mapper.EquipmentMapper;
import com.hrp.efficiency.service.EquipmentService;
import com.hrp.common.entity.Equipment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EquipmentServiceImpl implements EquipmentService {

    @Autowired
    private EquipmentMapper equipmentMapper;

    @Override
    public List<Equipment> getAll() {
        return equipmentMapper.selectAll();
    }

    @Override
    public List<Equipment> getByStatus(String status) {
        return equipmentMapper.selectByStatus(status);
    }

    @Override
    public Equipment getById(Long id) {
        return equipmentMapper.selectById(id);
    }

    @Override
    @Transactional
    public boolean save(Equipment equipment) {
        if (equipment.getEquipmentCode() == null || equipment.getEquipmentCode().isEmpty()) {
            equipment.setEquipmentCode("EQ" + System.currentTimeMillis());
        }
        if (equipment.getStatus() == null || equipment.getStatus().isEmpty()) {
            equipment.setStatus("NORMAL");
        }
        return equipmentMapper.insert(equipment) > 0;
    }

    @Override
    @Transactional
    public boolean update(Equipment equipment) {
        return equipmentMapper.updateById(equipment) > 0;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        return equipmentMapper.deleteById(id) > 0;
    }
}

