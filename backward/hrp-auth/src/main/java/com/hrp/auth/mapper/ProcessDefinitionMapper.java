package com.hrp.auth.mapper;

import com.hrp.common.entity.ProcessDefinition;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 流程定义数据访问接口
 */
public interface ProcessDefinitionMapper {
    /**
     * 根据ID查询流程定义
     */
    ProcessDefinition selectById(@Param("definitionId") Long definitionId);

    /**
     * 根据KEY查询流程定义
     */
    ProcessDefinition selectByKey(@Param("definitionKey") String definitionKey);

    /**
     * 根据类型查询流程定义列表
     */
    List<ProcessDefinition> selectByType(@Param("definitionType") String definitionType);

    /**
     * 查询所有流程定义
     */
    List<ProcessDefinition> selectAll();

    /**
     * 插入流程定义
     */
    int insert(ProcessDefinition definition);

    /**
     * 更新流程定义
     */
    int updateById(ProcessDefinition definition);

    /**
     * 删除流程定义
     */
    int deleteById(@Param("definitionId") Long definitionId);
}



