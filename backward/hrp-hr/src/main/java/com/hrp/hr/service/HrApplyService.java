package com.hrp.hr.service;

import com.hrp.common.entity.HrApply;
import java.util.List;

/**
 * 业务申请服务接口（参照报账模块）
 */
public interface HrApplyService {
    /**
     * 我的申请列表
     */
    List<HrApply> getMyApplies(Long empId);
    
    /**
     * 我的申请列表（分页）
     */
    com.hrp.common.entity.PageResult<HrApply> getMyAppliesPage(Long empId, Long page, Long size);
    
    /**
     * 根据状态查询申请列表（用于审批）
     */
    List<HrApply> getAppliesByStatus(String status);
    
    /**
     * 根据状态查询申请列表（分页）
     */
    com.hrp.common.entity.PageResult<HrApply> getAppliesByStatusPage(String status, Long page, Long size);
    
    /**
     * 我的审批列表
     */
    List<HrApply> getMyApprovalApplies(String userId);
    
    /**
     * 我的审批列表（分页）
     */
    com.hrp.common.entity.PageResult<HrApply> getMyApprovalAppliesPage(String userId, Long page, Long size);
    
    /**
     * 查询所有申请
     */
    List<HrApply> getAllApplies();
    
    /**
     * 查询所有申请（分页）
     */
    com.hrp.common.entity.PageResult<HrApply> getAllAppliesPage(Long page, Long size);
    
    /**
     * 根据ID查询
     */
    HrApply getById(Long applyId);
    
    /**
     * 根据申请编码查询
     */
    HrApply getByApplyNo(String applyNo);
    
    /**
     * 分页查询（支持多条件）
     */
    com.hrp.common.entity.PageResult<HrApply> getPage(Long page, Long size, String applyNo, 
                                                      Long empId, String hrApplyType, String status,
                                                      String startDate, String endDate);
    
    /**
     * 分页查询（审批人视角）
     */
    com.hrp.common.entity.PageResult<HrApply> getPageByApprover(Long page, Long size, String currentUserId,
                                                                 String applyNo, String empName, String hrApplyType, 
                                                                 String status, String startDate, String endDate);
    
    /**
     * 保存业务申请（草稿）
     */
    boolean save(HrApply hrApply);
    
    /**
     * 更新业务申请
     */
    boolean update(HrApply hrApply);
    
    /**
     * 删除业务申请
     */
    boolean delete(Long applyId);
    
    /**
     * 提交申请（启动流程）
     */
    boolean submit(Long applyId);
    
    /**
     * 撤回申请
     */
    boolean withdraw(Long applyId);
    
    /**
     * 同意审批
     */
    boolean approve(Long applyId, String userId, String opinion, String approverSignature);
    
    /**
     * 拒绝审批
     */
    boolean reject(Long applyId, String userId, String opinion);
    
    /**
     * 退回申请
     */
    boolean returnApply(Long applyId, String returnType, String opinion);
    
    /**
     * 生成申请编码
     */
    String generateApplyNo(String hrApplyType);
}
