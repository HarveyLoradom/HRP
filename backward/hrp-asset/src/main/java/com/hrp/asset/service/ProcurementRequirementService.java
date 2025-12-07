package com.hrp.asset.service;

import com.hrp.common.entity.ProcurementRequirement;

import java.util.List;

public interface ProcurementRequirementService {
    List<ProcurementRequirement> getAll();
    List<ProcurementRequirement> getByStatus(String status);
    ProcurementRequirement getById(Long id);
    boolean save(ProcurementRequirement requirement);
    boolean update(ProcurementRequirement requirement);
    boolean delete(Long id);
}

