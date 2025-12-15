package com.hrp.auth.mapper;

import com.hrp.common.entity.ProcessInstance;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 流程实例数据访问接口
 */
public interface ProcessInstanceMapper {
    /**
     * 根据ID查询流程实例
     */
    ProcessInstance selectById(@Param("instanceId") Long instanceId);

    /**
     * 根据业务主键查询流程实例
     */
    ProcessInstance selectByBusinessKey(@Param("businessKey") String businessKey);

    /**
     * 根据业务类型和业务ID查询流程实例
     */
    ProcessInstance selectByBusiness(@Param("businessType") String businessType, @Param("businessId") Long businessId);

    /**
     * 查询所有流程实例
     */
    List<ProcessInstance> selectAll();

    /**
     * 根据状态查询流程实例
     */
    List<ProcessInstance> selectByStatus(@Param("processStatus") String processStatus);

    /**
     * 分页查询所有流程实例
     */
    List<ProcessInstance> selectAllPage(@Param("offset") Long offset, @Param("size") Long size);

    /**
     * 统计所有流程实例总数
     */
    Long countAll();

    /**
     * 根据状态分页查询流程实例
     */
    List<ProcessInstance> selectByStatusPage(@Param("processStatus") String processStatus, @Param("offset") Long offset, @Param("size") Long size);

    /**
     * 统计指定状态的流程实例总数
     */
    Long countByStatus(@Param("processStatus") String processStatus);

    /**
     * 插入流程实例
     */
    int insert(ProcessInstance instance);

    /**
     * 更新流程实例
     */
    int updateById(ProcessInstance instance);

    /**
     * 删除流程实例
     */
    int deleteById(@Param("instanceId") Long instanceId);
}



