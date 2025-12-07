package com.hrp.efficiency.service.impl;

import com.hrp.efficiency.mapper.IncomeEquipmentMapper;
import com.hrp.efficiency.service.IncomeEquipmentService;
import com.hrp.common.entity.IncomeEquipment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class IncomeEquipmentServiceImpl implements IncomeEquipmentService {

    @Autowired
    private IncomeEquipmentMapper incomeEquipmentMapper;

    @Override
    public List<IncomeEquipment> getAll() {
        return incomeEquipmentMapper.selectAll();
    }

    @Override
    public List<IncomeEquipment> getByEquipmentId(Long equipmentId) {
        return incomeEquipmentMapper.selectByEquipmentId(equipmentId);
    }

    @Override
    public List<IncomeEquipment> getByDateRange(LocalDate startDate, LocalDate endDate) {
        return incomeEquipmentMapper.selectByDateRange(startDate, endDate);
    }

    @Override
    public IncomeEquipment getById(Long id) {
        return incomeEquipmentMapper.selectById(id);
    }

    @Override
    @Transactional
    public boolean save(IncomeEquipment incomeEquipment) {
        if (incomeEquipment.getIncomeNo() == null || incomeEquipment.getIncomeNo().isEmpty()) {
            incomeEquipment.setIncomeNo("EQINCOME" + System.currentTimeMillis());
        }
        if (incomeEquipment.getIncomeDate() == null) {
            incomeEquipment.setIncomeDate(LocalDate.now());
        }
        return incomeEquipmentMapper.insert(incomeEquipment) > 0;
    }

    @Override
    @Transactional
    public boolean update(IncomeEquipment incomeEquipment) {
        return incomeEquipmentMapper.updateById(incomeEquipment) > 0;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        return incomeEquipmentMapper.deleteById(id) > 0;
    }
}

