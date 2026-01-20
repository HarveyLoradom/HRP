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
     * @param isActive 启用状态：0-停用，1-启用，null-仅启用
     */
    List<ProcessDefinition> selectByType(@Param("definitionType") String definitionType, @Param("isActive") Long isActive);

    /**
     * 查询所有流程定义
     * @param isActive 启用状态：0-停用，1-启用，null-仅启用
     */
    List<ProcessDefinition> selectAll(@Param("isActive") Long isActive);

    /**
     * 分页查询所有流程定义
     * @param isActive 启用状态：0-停用，1-启用，null-仅启用
     */
    List<ProcessDefinition> selectAllPage(@Param("isActive") Long isActive, @Param("offset") Long offset, @Param("size") Long size);

    /**
     * 统计所有流程定义总数
     * @param isActive 启用状态：0-停用，1-启用，null-仅启用
     */
    Long countAll(@Param("isActive") Long isActive);

    /**
     * 根据类型分页查询流程定义
     * @param isActive 启用状态：0-停用，1-启用，null-仅启用
     */
    List<ProcessDefinition> selectByTypePage(@Param("definitionType") String definitionType, @Param("isActive") Long isActive, @Param("offset") Long offset, @Param("size") Long size);

    /**
     * 统计指定类型的流程定义总数
     * @param isActive 启用状态：0-停用，1-启用，null-仅启用
     */
    Long countByType(@Param("definitionType") String definitionType, @Param("isActive") Long isActive);

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



