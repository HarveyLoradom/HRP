package com.hrp.auth.mapper;

import com.hrp.common.entity.Code;
import org.apache.ibatis.annotations.Param;

/**
 * 系统字典数据访问接口
 */
public interface CodeMapper {
    /**
     * 根据ID查询字典
     */
    Code selectById(@Param("id") String id);
}




