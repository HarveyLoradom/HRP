package com.hrp.contract.service;

import com.hrp.common.entity.PactMain;

import java.util.List;

public interface PactMainService {
    List<PactMain> getAll();
    com.hrp.common.entity.PageResult<PactMain> getAllPage(Long page, Long size);
    List<PactMain> getByStatus(String status);
    com.hrp.common.entity.PageResult<PactMain> getByStatusPage(String status, Long page, Long size);
    List<PactMain> getMyApprovalContracts(String userId);
    com.hrp.common.entity.PageResult<PactMain> getPage(Long page, Long size, String contractNo, String contractName, String contractType, String status, String startDate, String endDate, Long empId);
    com.hrp.common.entity.PageResult<PactMain> getPageByApprover(Long page, Long size, String currentUserId, String contractNo, String contractName, String contractType, String status, String startDate, String endDate);
    PactMain getById(Long id);
    PactMain getByContractNo(String contractNo);
    PactMain save(PactMain pactMain);
    boolean update(PactMain pactMain);
    boolean submitByContractNo(String contractNo);
    boolean delete(Long id);
    boolean submit(Long contractId);
    boolean withdraw(Long contractId);
    boolean approve(Long contractId, String userId, String opinion, String signature);
    boolean reject(Long contractId, String userId, String opinion);
    boolean returnContract(Long contractId, String returnType, String opinion);
    boolean archive(Long contractId);
    String getNextApprover(Long contractId);
    
    /**
     * 查询已审批的合同（用于合同执行页面）
     */
    com.hrp.common.entity.PageResult<PactMain> getApprovedContractsPage(Long page, Long size, String contractNo, String contractName, String contractType, String executionStatus);
    
    /**
     * 失效合同（手动修改，设置 is_manual_modify = 1）
     */
    boolean invalidate(Long contractId);
    
    /**
     * 归档合同（手动修改，设置 is_manual_modify = 1）
     */
    boolean archiveManual(Long contractId);
    
    /**
     * 手动触发更新合同执行状态
     * 根据开始时间和结束时间自动更新执行状态（与定时任务相同的逻辑）
     */
    void updateExecutionStatus();
}



