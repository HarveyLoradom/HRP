package com.hrp.auth.service;

import com.hrp.common.entity.ProcessDefinition;

import java.util.List;

/**
 * 流程定义服务接口
 */
public interface ProcessDefinitionService {
    /**
     * 根据ID查询流程定义
     */
    ProcessDefinition getById(Long definitionId);

    /**
     * 根据KEY查询流程定义
     */
    ProcessDefinition getByKey(String definitionKey);

    /**
     * 根据类型查询流程定义列表
     */
    List<ProcessDefinition> getByType(String definitionType);

    /**
     * 查询所有流程定义
     */
    List<ProcessDefinition> getAll();

    /**
     * 新增流程定义
     */
    boolean save(ProcessDefinition definition);

    /**
     * 更新流程定义
     */
    boolean update(ProcessDefinition definition);

    /**
     * 删除流程定义
     */
    boolean delete(Long definitionId);
}



