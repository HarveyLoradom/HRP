package com.hrp.auth.mapper;

import com.hrp.common.entity.ProcessVariable;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 流程变量数据访问接口
 */
public interface ProcessVariableMapper {
    /**
     * 根据流程实例ID查询变量列表
     */
    List<ProcessVariable> selectByInstanceId(@Param("processInstanceId") Long processInstanceId);

    /**
     * 根据流程实例ID和变量KEY查询变量
     */
    ProcessVariable selectByInstanceIdAndKey(@Param("processInstanceId") Long processInstanceId, @Param("variableKey") String variableKey);

    /**
     * 插入流程变量
     */
    int insert(ProcessVariable variable);

    /**
     * 更新流程变量
     */
    int updateById(ProcessVariable variable);

    /**
     * 删除流程变量
     */
    int deleteById(@Param("variableId") Long variableId);

    /**
     * 根据流程实例ID删除所有变量
     */
    int deleteByInstanceId(@Param("processInstanceId") Long instanceId);
}



