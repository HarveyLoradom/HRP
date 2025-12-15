package com.hrp.auth.service.impl;

import com.hrp.auth.mapper.ProcessTaskMapper;
import com.hrp.auth.service.ProcessTaskService;
import com.hrp.common.entity.ProcessTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 流程任务服务实现类
 */
@Service
public class ProcessTaskServiceImpl implements ProcessTaskService {

    @Autowired
    private ProcessTaskMapper processTaskMapper;

    @Override
    public ProcessTask getById(Long taskId) {
        return processTaskMapper.selectById(taskId);
    }

    @Override
    public List<ProcessTask> getByInstanceId(Long processInstanceId) {
        return processTaskMapper.selectByInstanceId(processInstanceId);
    }

    @Override
    public List<ProcessTask> getByAssignee(String assigneeUserId) {
        return processTaskMapper.selectByAssignee(assigneeUserId);
    }

    @Override
    public List<ProcessTask> getByBusinessKey(String businessKey) {
        return processTaskMapper.selectByBusinessKey(businessKey);
    }

    @Override
    public List<ProcessTask> getByStatus(String taskStatus) {
        return processTaskMapper.selectByStatus(taskStatus);
    }

    @Override
    public com.hrp.common.entity.PageResult<ProcessTask> getByStatusPage(String taskStatus, Long page, Long size) {
        Long offset = (page - 1) * size;
        List<ProcessTask> records = processTaskMapper.selectByStatusPage(taskStatus, offset, size);
        Long total = processTaskMapper.countByStatus(taskStatus);
        return com.hrp.common.entity.PageResult.of(records, total, size, page);
    }

    @Override
    public com.hrp.common.entity.PageResult<ProcessTask> getByBusinessKeyPage(String businessKey, Long page, Long size) {
        Long offset = (page - 1) * size;
        List<ProcessTask> records = processTaskMapper.selectByBusinessKeyPage(businessKey, offset, size);
        Long total = processTaskMapper.countByBusinessKey(businessKey);
        return com.hrp.common.entity.PageResult.of(records, total, size, page);
    }

    @Override
    @Transactional
    public boolean transferTask(Long taskId, String newAssigneeUserId, String newAssigneeUserName, String newAssigneeEmpCode) {
        ProcessTask task = processTaskMapper.selectById(taskId);
        if (task == null) {
            return false;
        }
        task.setAssigneeUserId(newAssigneeUserId);
        task.setAssigneeUserName(newAssigneeUserName);
        task.setAssigneeEmpCode(newAssigneeEmpCode);
        task.setTaskStatus("TRANSFERRED");
        task.setUpdateTime(LocalDateTime.now());
        return processTaskMapper.updateById(task) > 0;
    }
}



