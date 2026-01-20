package com.hrp.auth.mapper.impl;

import com.hrp.auth.mapper.ProcessTaskMapper;
import com.hrp.common.entity.ProcessTask;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 流程任务数据访问实现类
 */
@Repository
public class ProcessTaskMapperImpl implements ProcessTaskMapper {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    private static final String NAMESPACE = "com.hrp.auth.mapper.ProcessTaskMapper";

    @Override
    public ProcessTask selectById(String taskId) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", taskId);
    }

    @Override
    public List<ProcessTask> selectByInstanceId(Long processInstanceId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByInstanceId", processInstanceId);
    }

    @Override
    public List<ProcessTask> selectByAssignee(String assigneeUserId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByAssignee", assigneeUserId);
    }

    @Override
    public List<ProcessTask> selectByAssigneeAndStatus(String assigneeUserId, String taskStatus) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("assigneeUserId", assigneeUserId);
        params.put("taskStatus", taskStatus);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByAssigneeAndStatus", params);
    }

    @Override
    public List<ProcessTask> selectByBusinessKey(String businessKey) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByBusinessKey", businessKey);
    }

    @Override
    public List<ProcessTask> selectByStatus(String taskStatus) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByStatus", taskStatus);
    }

    @Override
    public List<ProcessTask> selectByStatusPage(String taskStatus, Long offset, Long size) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("taskStatus", taskStatus);
        params.put("offset", offset);
        params.put("size", size);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByStatusPage", params);
    }

    @Override
    public Long countByStatus(String taskStatus) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".countByStatus", taskStatus);
    }

    @Override
    public List<ProcessTask> selectByBusinessKeyPage(String businessKey, Long offset, Long size) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("businessKey", businessKey);
        params.put("offset", offset);
        params.put("size", size);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByBusinessKeyPage", params);
    }

    @Override
    public Long countByBusinessKey(String businessKey) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".countByBusinessKey", businessKey);
    }

    @Override
    public int insert(ProcessTask task) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", task);
    }

    @Override
    public int updateById(ProcessTask task) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", task);
    }

    @Override
    public int deleteById(String taskId) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", taskId);
    }

    @Override
    public ProcessTask selectNextPendingTask(Long processInstanceId) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectNextPendingTask", processInstanceId);
    }
    
    @Override
    public List<ProcessTask> selectByTaskKey(String taskKey) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByTaskKey", taskKey);
    }
    
    @Override
    public int deleteByTaskKey(String taskKey) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteByTaskKey", taskKey);
    }
    
    @Override
    public int terminateTasksByTaskKey(String taskKey) {
        return sqlSessionTemplate.update(NAMESPACE + ".terminateTasksByTaskKey", taskKey);
    }
    
    @Override
    public List<ProcessTask> selectAllCurrentTasks() {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAllCurrentTasks");
    }
}



