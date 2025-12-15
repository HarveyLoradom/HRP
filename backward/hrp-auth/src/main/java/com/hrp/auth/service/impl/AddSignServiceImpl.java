package com.hrp.auth.service.impl;

import com.hrp.auth.mapper.AddSignRecordMapper;
import com.hrp.auth.mapper.ProcessTaskMapper;
import com.hrp.auth.service.AddSignService;
import com.hrp.common.entity.AddSignRecord;
import com.hrp.common.entity.ProcessTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 加签服务实现类
 */
@Service
public class AddSignServiceImpl implements AddSignService {

    @Autowired
    private AddSignRecordMapper addSignRecordMapper;

    @Autowired
    private ProcessTaskMapper processTaskMapper;

    @Override
    @Transactional
    public Long createAddSign(Long parentTaskId, String targetUserId, String targetUserName, String targetEmpCode,
                               String addsignUserId, String addsignUserName, String addsignEmpCode, String addsignReason) {
        // 1. 查询父任务
        ProcessTask parentTask = processTaskMapper.selectById(parentTaskId);
        if (parentTask == null || !"PENDING".equals(parentTask.getTaskStatus())) {
            throw new RuntimeException("父任务不存在或不是待办理状态");
        }

        // 2. 创建加签记录
        AddSignRecord record = new AddSignRecord();
        record.setProcessInstanceId(parentTask.getProcessInstanceId());
        record.setParentTaskId(parentTaskId);
        record.setAddsignUserId(addsignUserId);
        record.setAddsignUserName(addsignUserName);
        record.setAddsignEmpCode(addsignEmpCode);
        record.setTargetUserId(targetUserId);
        record.setTargetUserName(targetUserName);
        record.setTargetEmpCode(targetEmpCode);
        record.setAddsignReason(addsignReason);
        record.setAddsignStatus("PENDING");
        addSignRecordMapper.insert(record);

        // 3. 创建加签任务
        ProcessTask addsignTask = new ProcessTask();
        addsignTask.setProcessInstanceId(parentTask.getProcessInstanceId());
        addsignTask.setTaskKey("addsign_" + parentTask.getTaskKey());
        addsignTask.setTaskName("加签审批：" + parentTask.getTaskName());
        addsignTask.setAssigneeUserId(targetUserId);
        addsignTask.setAssigneeUserName(targetUserName);
        addsignTask.setAssigneeEmpCode(targetEmpCode);
        addsignTask.setTaskStatus("PENDING");
        addsignTask.setTaskType("APPROVE");
        addsignTask.setApprovalType("SINGLE");
        addsignTask.setParentTaskId(parentTaskId);
        addsignTask.setIsAddsignTask(1);
        addsignTask.setPriority(parentTask.getPriority());
        processTaskMapper.insert(addsignTask);

        // 4. 更新加签记录的任务ID
        record.setAddsignTaskId(addsignTask.getTaskId());
        addSignRecordMapper.updateById(record);

        // 5. 暂停父任务（等待加签任务完成）
        parentTask.setTaskStatus("SUSPENDED");
        processTaskMapper.updateById(parentTask);

        return record.getAddsignId();
    }

    @Override
    @Transactional
    public boolean completeAddSign(Long addsignTaskId, String approvalResult, String approvalOpinion) {
        // 1. 查询加签任务
        ProcessTask addsignTask = processTaskMapper.selectById(addsignTaskId);
        if (addsignTask == null || addsignTask.getIsAddsignTask() == null || addsignTask.getIsAddsignTask() != 1) {
            return false;
        }

        // 2. 查询加签记录
        AddSignRecord record = addSignRecordMapper.selectByAddsignTaskId(addsignTaskId);
        if (record == null) {
            return false;
        }

        // 3. 更新加签任务状态
        addsignTask.setTaskStatus("COMPLETED");
        addsignTask.setComment(approvalOpinion);
        addsignTask.setCompleteTime(LocalDateTime.now());
        processTaskMapper.updateById(addsignTask);

        // 4. 更新加签记录状态
        record.setAddsignStatus("COMPLETED");
        record.setCompleteTime(LocalDateTime.now());
        addSignRecordMapper.updateById(record);

        // 5. 恢复父任务（如果加签人同意，继续原任务；如果拒绝，可能需要特殊处理）
        ProcessTask parentTask = processTaskMapper.selectById(record.getParentTaskId());
        if (parentTask != null && "SUSPENDED".equals(parentTask.getTaskStatus())) {
            if ("APPROVED".equals(approvalResult)) {
                // 加签人同意，恢复父任务
                parentTask.setTaskStatus("PENDING");
                processTaskMapper.updateById(parentTask);
            } else {
                // 加签人拒绝，可能需要终止流程或通知父任务办理人
                // 这里可以根据业务需求决定如何处理
                parentTask.setTaskStatus("PENDING");
                processTaskMapper.updateById(parentTask);
            }
        }

        return true;
    }

    @Override
    public List<AddSignRecord> getAddSignRecordsByParentTaskId(Long parentTaskId) {
        return addSignRecordMapper.selectByParentTaskId(parentTaskId);
    }
}

