package com.hrp.contract.service;

import com.hrp.common.entity.PactMain;

import java.util.List;

public interface PactMainService {
    List<PactMain> getAll();
    List<PactMain> getByStatus(String status);
    PactMain getById(Long id);
    PactMain getByContractNo(String contractNo);
    boolean save(PactMain pactMain);
    boolean update(PactMain pactMain);
    boolean delete(Long id);
}

