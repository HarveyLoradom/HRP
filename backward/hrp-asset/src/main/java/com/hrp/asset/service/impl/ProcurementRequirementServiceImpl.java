package com.hrp.asset.service.impl;

import com.hrp.asset.mapper.ProcurementRequirementMapper;
import com.hrp.asset.service.ProcurementRequirementService;
import com.hrp.common.entity.ProcurementRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProcurementRequirementServiceImpl implements ProcurementRequirementService {

    @Autowired
    private ProcurementRequirementMapper procurementRequirementMapper;

    @Override
    public List<ProcurementRequirement> getAll() {
        return procurementRequirementMapper.selectAll();
    }

    @Override
    public List<ProcurementRequirement> getByStatus(String status) {
        return procurementRequirementMapper.selectByStatus(status);
    }

    @Override
    public ProcurementRequirement getById(Long id) {
        return procurementRequirementMapper.selectById(id);
    }

    @Override
    @Transactional
    public boolean save(ProcurementRequirement requirement) {
        if (requirement.getRequirementNo() == null || requirement.getRequirementNo().isEmpty()) {
            requirement.setRequirementNo("PROC" + System.currentTimeMillis());
        }
        if (requirement.getStatus() == null || requirement.getStatus().isEmpty()) {
            requirement.setStatus("PENDING");
        }
        return procurementRequirementMapper.insert(requirement) > 0;
    }

    @Override
    @Transactional
    public boolean update(ProcurementRequirement requirement) {
        return procurementRequirementMapper.updateById(requirement) > 0;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        return procurementRequirementMapper.deleteById(id) > 0;
    }
}

