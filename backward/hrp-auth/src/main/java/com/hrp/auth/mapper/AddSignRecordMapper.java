package com.hrp.auth.mapper;

import com.hrp.common.entity.AddSignRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 加签记录数据访问接口
 */
public interface AddSignRecordMapper {
    /**
     * 根据ID查询加签记录
     */
    AddSignRecord selectById(@Param("addsignId") Long addsignId);

    /**
     * 根据父任务ID查询加签记录
     */
    List<AddSignRecord> selectByParentTaskId(@Param("parentTaskId") Long parentTaskId);

    /**
     * 根据流程实例ID查询加签记录
     */
    List<AddSignRecord> selectByProcessInstanceId(@Param("processInstanceId") Long processInstanceId);

    /**
     * 根据加签任务ID查询加签记录
     */
    AddSignRecord selectByAddsignTaskId(@Param("addsignTaskId") Long addsignTaskId);

    /**
     * 插入加签记录
     */
    int insert(AddSignRecord record);

    /**
     * 更新加签记录
     */
    int updateById(AddSignRecord record);
}

