package com.hrp.contract.service.impl;

import com.hrp.contract.mapper.PactMainMapper;
import com.hrp.contract.service.PactMainService;
import com.hrp.common.entity.PactMain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PactMainServiceImpl implements PactMainService {

    @Autowired
    private PactMainMapper pactMainMapper;

    @Override
    public List<PactMain> getAll() {
        return pactMainMapper.selectAll();
    }

    @Override
    public List<PactMain> getByStatus(String status) {
        return pactMainMapper.selectByStatus(status);
    }

    @Override
    public PactMain getById(Long id) {
        return pactMainMapper.selectById(id);
    }

    @Override
    public PactMain getByContractNo(String contractNo) {
        return pactMainMapper.selectByContractNo(contractNo);
    }

    @Override
    @Transactional
    public boolean save(PactMain pactMain) {
        if (pactMain.getContractNo() == null || pactMain.getContractNo().isEmpty()) {
            pactMain.setContractNo("CONTRACT" + System.currentTimeMillis());
        }
        if (pactMain.getStatus() == null || pactMain.getStatus().isEmpty()) {
            pactMain.setStatus("DRAFT");
        }
        return pactMainMapper.insert(pactMain) > 0;
    }

    @Override
    @Transactional
    public boolean update(PactMain pactMain) {
        return pactMainMapper.updateById(pactMain) > 0;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        return pactMainMapper.deleteById(id) > 0;
    }
}

