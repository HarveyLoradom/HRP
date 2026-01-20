package com.hrp.asset.service;

import com.hrp.common.entity.AssetPurchaseApplyMain;
import com.hrp.common.entity.AssetPurchaseApplyDetail;

import java.util.List;

/**
 * 采购申请服务接口
 */
public interface AssetPurchaseApplyService {
    // 查询方法
    AssetPurchaseApplyMain getById(Long id);
    AssetPurchaseApplyMain getByApplyNo(String applyNo);
    List<AssetPurchaseApplyMain> getByEmpId(Long empId);
    List<AssetPurchaseApplyMain> getByStatus(String status);
    List<AssetPurchaseApplyMain> getMyApprovalApplies(String userId);
    com.hrp.common.entity.PageResult<AssetPurchaseApplyMain> getPage(Long page, Long size, String applyNo, String applyEmpName, String status, String startDate, String endDate);
    com.hrp.common.entity.PageResult<AssetPurchaseApplyMain> getPageByApprover(Long page, Long size, String currentUserId, String applyNo, String applyEmpName, String status, String startDate, String endDate);
    
    // 保存和更新方法
    AssetPurchaseApplyMain save(AssetPurchaseApplyMain apply, List<AssetPurchaseApplyDetail> details);
    AssetPurchaseApplyMain update(AssetPurchaseApplyMain apply, List<AssetPurchaseApplyDetail> details);
    boolean delete(Long id);
    
    // 流程相关方法
    boolean submit(Long applyId);
    boolean withdraw(Long applyId);
    boolean approve(Long applyId, String userId, String opinion, String approverSignature);
    boolean reject(Long applyId, String userId, String opinion);
    boolean returnApply(Long applyId, String returnType, String opinion);
    
    // 明细相关方法
    List<AssetPurchaseApplyDetail> getDetailsByApplyId(Long applyId);
}

