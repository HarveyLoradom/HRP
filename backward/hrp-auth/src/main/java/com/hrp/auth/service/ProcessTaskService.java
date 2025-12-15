package com.hrp.auth.service;

import com.hrp.common.entity.ProcessTask;

import java.util.List;

/**
 * 流程任务服务接口
 */
public interface ProcessTaskService {
    /**
     * 根据ID查询流程任务
     */
    ProcessTask getById(Long taskId);

    /**
     * 根据流程实例ID查询任务列表
     */
    List<ProcessTask> getByInstanceId(Long processInstanceId);

    /**
     * 根据办理人ID查询任务列表
     */
    List<ProcessTask> getByAssignee(String assigneeUserId);

    /**
     * 根据业务主键查询任务列表
     */
    List<ProcessTask> getByBusinessKey(String businessKey);

    /**
     * 根据任务状态查询任务列表
     */
    List<ProcessTask> getByStatus(String taskStatus);

    /**
     * 根据任务状态分页查询任务列表
     */
    com.hrp.common.entity.PageResult<ProcessTask> getByStatusPage(String taskStatus, Long page, Long size);

    /**
     * 根据业务主键分页查询任务列表
     */
    com.hrp.common.entity.PageResult<ProcessTask> getByBusinessKeyPage(String businessKey, Long page, Long size);

    /**
     * 转办任务（更改办理人）
     */
    boolean transferTask(Long taskId, String newAssigneeUserId, String newAssigneeUserName, String newAssigneeEmpCode);
}



