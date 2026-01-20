package com.hrp.hr.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 精简员工查询（用于全员统计）
 */
public interface SysEmpMapper {
    /**
     * 查询所有未停用员工ID
     */
    List<Long> selectActiveEmpIds();

    /**
     * 根据员工编码查询员工ID
     */
    Long selectEmpIdByCode(@Param("empCode") String empCode);
}


