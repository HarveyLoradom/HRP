package com.hrp.asset.service;

import com.hrp.common.entity.ProcurementRequirement;
import com.hrp.common.entity.ProcurementRequirementDTO;

import java.util.List;

public interface ProcurementRequirementService {
    List<ProcurementRequirement> getAll();
    List<ProcurementRequirement> getByStatus(String status);
    List<ProcurementRequirement> getByApplicant(Long applicantId);
    List<ProcurementRequirement> getByApprover(String approverId);
    List<ProcurementRequirement> getMyList(String userId, Long empId);
    ProcurementRequirement getById(Long id);
    ProcurementRequirementDTO getDetail(Long id);
    boolean save(ProcurementRequirementDTO dto);
    boolean submit(Long requirementId);
    boolean withdraw(Long requirementId);
    boolean approve(Long requirementId, String userId, String opinion);
    boolean reject(Long requirementId, String userId, String opinion);
    boolean delete(Long id);
}

