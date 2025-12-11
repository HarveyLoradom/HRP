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
        PactMain contract = pactMainMapper.selectById(id);
        if (contract == null) {
            return false;
        }
        // 只有草稿状态才能删除
        if (!"DRAFT".equals(contract.getStatus())) {
            return false;
        }
        return pactMainMapper.deleteById(id) > 0;
    }

    @Override
    public List<PactMain> getMyApprovalContracts(String userId) {
        return pactMainMapper.selectByApprover(userId);
    }

    @Override
    @Transactional
    public boolean submit(Long contractId) {
        PactMain contract = pactMainMapper.selectById(contractId);
        if (contract == null) {
            return false;
        }
        if (!"DRAFT".equals(contract.getStatus())) {
            return false; // 只有草稿状态才能提交
        }
        contract.setStatus("PENDING");
        // TODO: 启动流程实例
        return pactMainMapper.updateById(contract) > 0;
    }

    @Override
    @Transactional
    public boolean withdraw(Long contractId) {
        PactMain contract = pactMainMapper.selectById(contractId);
        if (contract == null) {
            return false;
        }
        if (!"PENDING".equals(contract.getStatus())) {
            return false; // 只有待审批状态才能撤回
        }
        contract.setStatus("DRAFT");
        // TODO: 取消流程实例
        return pactMainMapper.updateById(contract) > 0;
    }

    @Override
    @Transactional
    public boolean approve(Long contractId, String userId, String opinion) {
        PactMain contract = pactMainMapper.selectById(contractId);
        if (contract == null) {
            return false;
        }
        // TODO: 处理审批逻辑，更新流程任务，判断是否流程结束
        contract.setStatus("APPROVED");
        contract.setRemark(opinion);
        return pactMainMapper.updateById(contract) > 0;
    }

    @Override
    @Transactional
    public boolean reject(Long contractId, String userId, String opinion) {
        PactMain contract = pactMainMapper.selectById(contractId);
        if (contract == null) {
            return false;
        }
        // TODO: 处理驳回逻辑，结束流程
        contract.setStatus("REJECTED");
        contract.setRemark(opinion);
        return pactMainMapper.updateById(contract) > 0;
    }

    @Override
    @Transactional
    public boolean archive(Long contractId) {
        PactMain contract = pactMainMapper.selectById(contractId);
        if (contract == null) {
            return false;
        }
        contract.setStatus("ARCHIVED");
        return pactMainMapper.updateById(contract) > 0;
    }

    @Override
    public String getNextApprover(Long contractId) {
        // TODO: 从流程任务中获取下一个审批人
        return "待实现";
    }
}

