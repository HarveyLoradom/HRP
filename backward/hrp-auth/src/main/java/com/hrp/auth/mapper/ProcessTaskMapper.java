package com.hrp.auth.mapper;

import com.hrp.common.entity.ProcessTask;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 流程任务数据访问接口
 */
public interface ProcessTaskMapper {
    /**
     * 根据ID查询流程任务
     */
    ProcessTask selectById(@Param("taskId") Long taskId);

    /**
     * 根据流程实例ID查询任务列表
     */
    List<ProcessTask> selectByInstanceId(@Param("processInstanceId") Long processInstanceId);

    /**
     * 根据办理人ID查询任务列表
     */
    List<ProcessTask> selectByAssignee(@Param("assigneeUserId") String assigneeUserId);

    /**
     * 根据业务主键查询任务列表
     */
    List<ProcessTask> selectByBusinessKey(@Param("businessKey") String businessKey);

    /**
     * 根据任务状态查询任务列表
     */
    List<ProcessTask> selectByStatus(@Param("taskStatus") String taskStatus);

    /**
     * 插入流程任务
     */
    int insert(ProcessTask task);

    /**
     * 更新流程任务
     */
    int updateById(ProcessTask task);

    /**
     * 删除流程任务
     */
    int deleteById(@Param("taskId") Long taskId);
}



