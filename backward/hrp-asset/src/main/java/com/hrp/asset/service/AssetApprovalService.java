package com.hrp.asset.service;

import com.hrp.common.entity.AssetApproval;
import com.hrp.common.entity.AssetApprovalDTO;

import java.util.List;

public interface AssetApprovalService {
    List<AssetApproval> getAll();
    List<AssetApproval> getByType(String approvalType);
    List<AssetApproval> getByApplicant(Long applicantId);
    List<AssetApproval> getByApprover(String approverId);
    List<AssetApproval> getByStatus(String status);
    List<AssetApproval> getMyList(String userId, Long empId, String approvalType);
    com.hrp.common.entity.PageResult<AssetApproval> getMyListPage(String userId, Long empId, String approvalType, Long page, Long size);
    com.hrp.common.entity.PageResult<AssetApproval> getAllPage(Long page, Long size);
    AssetApproval getById(Long id);
    AssetApprovalDTO getDetail(Long id);
    boolean save(AssetApprovalDTO dto);
    boolean update(AssetApprovalDTO dto);
    boolean delete(Long id);
    boolean submit(Long approvalId);
    boolean withdraw(Long approvalId);
    boolean approve(Long approvalId, String userId, String opinion);
    boolean reject(Long approvalId, String userId, String opinion);
}
