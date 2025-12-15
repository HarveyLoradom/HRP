package com.hrp.auth.service;

import com.hrp.common.entity.ProcessInstance;
import com.hrp.common.entity.ProcessVariable;

import java.util.List;

/**
 * 流程实例服务接口
 */
public interface ProcessInstanceService {
    /**
     * 根据ID查询流程实例
     */
    ProcessInstance getById(Long instanceId);

    /**
     * 根据业务主键查询流程实例
     */
    ProcessInstance getByBusinessKey(String businessKey);

    /**
     * 根据业务类型和业务ID查询流程实例
     */
    ProcessInstance getByBusiness(String businessType, Long businessId);

    /**
     * 查询所有流程实例
     */
    List<ProcessInstance> getAll();

    /**
     * 根据状态查询流程实例
     */
    List<ProcessInstance> getByStatus(String processStatus);

    /**
     * 分页查询所有流程实例
     */
    com.hrp.common.entity.PageResult<ProcessInstance> getAllPage(Long page, Long size);

    /**
     * 根据状态分页查询流程实例
     */
    com.hrp.common.entity.PageResult<ProcessInstance> getByStatusPage(String processStatus, Long page, Long size);

    /**
     * 根据流程实例ID查询变量列表
     */
    List<ProcessVariable> getVariablesByInstanceId(Long instanceId);

    /**
     * 启动流程实例（旧方法，保留兼容）
     */
    boolean startProcess(ProcessInstance instance);

    /**
     * 启动流程实例（新方法，根据流程定义ID启动）
     */
    ProcessInstance startProcess(Long processDefinitionId, String businessKey, String businessType, Long businessId);
}



