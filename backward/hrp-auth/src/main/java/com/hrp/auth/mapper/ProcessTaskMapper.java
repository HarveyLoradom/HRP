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
    ProcessTask selectById(@Param("taskId") String taskId);

    /**
     * 根据流程实例ID查询任务列表
     */
    List<ProcessTask> selectByInstanceId(@Param("processInstanceId") Long processInstanceId);

    /**
     * 根据办理人ID查询任务列表
     */
    List<ProcessTask> selectByAssignee(@Param("assigneeUserId") String assigneeUserId);
    
    /**
     * 根据办理人ID和任务状态查询任务列表（用于查询已审批等历史记录）
     */
    List<ProcessTask> selectByAssigneeAndStatus(@Param("assigneeUserId") String assigneeUserId, @Param("taskStatus") String taskStatus);

    /**
     * 根据业务主键查询任务列表
     */
    List<ProcessTask> selectByBusinessKey(@Param("businessKey") String businessKey);

    /**
     * 根据任务状态查询任务列表
     */
    List<ProcessTask> selectByStatus(@Param("taskStatus") String taskStatus);

    /**
     * 根据任务状态分页查询任务列表
     */
    List<ProcessTask> selectByStatusPage(@Param("taskStatus") String taskStatus, @Param("offset") Long offset, @Param("size") Long size);

    /**
     * 统计指定状态的任务总数
     */
    Long countByStatus(@Param("taskStatus") String taskStatus);

    /**
     * 根据业务主键分页查询任务列表
     */
    List<ProcessTask> selectByBusinessKeyPage(@Param("businessKey") String businessKey, @Param("offset") Long offset, @Param("size") Long size);

    /**
     * 统计指定业务主键的任务总数
     */
    Long countByBusinessKey(@Param("businessKey") String businessKey);

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
    int deleteById(@Param("taskId") String taskId);

    /**
     * 根据流程实例ID查询下一个待处理的任务（按创建时间排序，取第一个）
     */
    ProcessTask selectNextPendingTask(@Param("processInstanceId") Long processInstanceId);
    
    /**
     * 根据taskKey查询任务列表（taskKey就是apply_no）
     */
    List<ProcessTask> selectByTaskKey(@Param("taskKey") String taskKey);
    
    /**
     * 根据taskKey删除所有任务记录
     */
    int deleteByTaskKey(@Param("taskKey") String taskKey);
    
    /**
     * 根据task_key终止所有PENDING状态的任务
     */
    int terminateTasksByTaskKey(@Param("taskKey") String taskKey);
    
    /**
     * 查询所有当前节点的任务（显示所有流程的当前审批节点）
     */
    List<ProcessTask> selectAllCurrentTasks();
}



