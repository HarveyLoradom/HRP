package com.hrp.hr.mapper;

import com.hrp.common.entity.HrSalCalculate;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 薪酬核算表数据访问接口
 */
public interface HrSalCalculateMapper {
    HrSalCalculate selectById(@Param("calcId") Long calcId);
    HrSalCalculate selectByEmpIdAndMonth(@Param("empId") Long empId, @Param("calcMonth") String calcMonth);
    List<HrSalCalculate> selectByConditions(@Param("empId") Long empId,
                                            @Param("empCode") String empCode,
                                            @Param("empName") String empName,
                                            @Param("calcMonth") String calcMonth,
                                            @Param("calcStatus") String calcStatus);
    int insert(HrSalCalculate calculate);
    int updateById(HrSalCalculate calculate);
    int deleteById(@Param("calcId") Long calcId);
}

