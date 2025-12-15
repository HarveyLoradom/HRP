package com.hrp.contract.service;

import com.hrp.common.entity.PactMain;

import java.util.List;

public interface PactMainService {
    List<PactMain> getAll();
    com.hrp.common.entity.PageResult<PactMain> getAllPage(Long page, Long size);
    List<PactMain> getByStatus(String status);
    com.hrp.common.entity.PageResult<PactMain> getByStatusPage(String status, Long page, Long size);
    List<PactMain> getMyApprovalContracts(String userId);
    PactMain getById(Long id);
    PactMain getByContractNo(String contractNo);
    boolean save(PactMain pactMain);
    boolean update(PactMain pactMain);
    boolean delete(Long id);
    boolean submit(Long contractId);
    boolean withdraw(Long contractId);
    boolean approve(Long contractId, String userId, String opinion);
    boolean reject(Long contractId, String userId, String opinion);
    boolean archive(Long contractId);
    String getNextApprover(Long contractId);
}

