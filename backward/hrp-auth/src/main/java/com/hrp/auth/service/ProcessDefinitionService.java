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
     * 分页查询所有流程定义
     */
    com.hrp.common.entity.PageResult<ProcessDefinition> getAllPage(Long page, Long size);

    /**
     * 根据类型分页查询流程定义
     */
    com.hrp.common.entity.PageResult<ProcessDefinition> getByTypePage(String definitionType, Long page, Long size);

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

    /**
     * 导出流程定义为JSON格式
     * 
     * @param definition 流程定义对象
     * @return JSON格式的流程定义内容
     */
    String exportDefinition(ProcessDefinition definition);

    /**
     * 导入流程定义
     * 
     * @param jsonContent JSON格式的流程定义内容
     * @param definitionKey 流程定义KEY（可选，如果为空则从JSON中读取）
     * @param definitionName 流程定义名称（可选，如果为空则从JSON中读取）
     * @param definitionType 流程定义类型（可选，如果为空则从JSON中读取）
     * @return 导入后的流程定义对象
     */
    ProcessDefinition importDefinition(String jsonContent, String definitionKey, String definitionName, String definitionType);
}



