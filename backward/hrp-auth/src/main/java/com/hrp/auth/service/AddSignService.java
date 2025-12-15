package com.hrp.auth.service;

import com.hrp.common.entity.AddSignRecord;

/**
 * 加签服务接口
 */
public interface AddSignService {
    /**
     * 创建加签任务
     * @param parentTaskId 父任务ID（被加签的任务）
     * @param targetUserId 被加签人ID
     * @param targetUserName 被加签人姓名
     * @param targetEmpCode 被加签人工号
     * @param addsignUserId 执行加签操作的用户ID
     * @param addsignUserName 执行加签操作的用户姓名
     * @param addsignEmpCode 执行加签操作的用户工号
     * @param addsignReason 加签原因
     * @return 加签记录ID
     */
    Long createAddSign(Long parentTaskId, String targetUserId, String targetUserName, String targetEmpCode,
                       String addsignUserId, String addsignUserName, String addsignEmpCode, String addsignReason);

    /**
     * 完成加签任务（加签人审批后，回到原任务）
     * @param addsignTaskId 加签任务ID
     * @param approvalResult 审批结果：APPROVED-同意，REJECTED-拒绝
     * @param approvalOpinion 审批意见
     * @return 是否成功
     */
    boolean completeAddSign(Long addsignTaskId, String approvalResult, String approvalOpinion);

    /**
     * 根据父任务ID查询加签记录
     */
    java.util.List<AddSignRecord> getAddSignRecordsByParentTaskId(Long parentTaskId);
}

