package com.hrp.hr.mapper;

import com.hrp.common.entity.HrSalConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 薪酬配置表数据访问接口
 */
public interface HrSalConfigMapper {
    HrSalConfig selectById(@Param("configId") Integer configId);
    HrSalConfig selectByEmpId(@Param("empId") Long empId);
    List<HrSalConfig> selectByConditions(@Param("empId") Long empId, @Param("empCode") String empCode, @Param("empName") String empName);
    int insert(HrSalConfig config);
    int updateById(HrSalConfig config);
    int deleteById(@Param("configId") Integer configId);
    int deleteByEmpId(@Param("empId") Long empId);
    /**
     * 获取所有已有薪酬配置的员工ID列表
     */
    List<Long> selectAllEmpIds();
}

