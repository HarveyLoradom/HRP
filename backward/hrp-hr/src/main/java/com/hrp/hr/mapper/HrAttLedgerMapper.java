package com.hrp.hr.mapper;

import com.hrp.common.entity.HrAttLedger;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 员工考勤台账表数据访问接口
 */
public interface HrAttLedgerMapper {
    HrAttLedger selectById(@Param("ledgerId") Long ledgerId);
    HrAttLedger selectByEmpIdAndMonth(@Param("empId") Long empId, @Param("attMonth") String attMonth);
    List<HrAttLedger> selectByConditions(@Param("empId") Long empId,
                                         @Param("attMonth") String attMonth,
                                         @Param("ledgerStatus") String ledgerStatus,
                                         @Param("empCode") String empCode,
                                         @Param("empName") String empName,
                                         @Param("deptId") Long deptId);
    int insert(HrAttLedger ledger);
    int updateById(HrAttLedger ledger);
    int deleteById(@Param("ledgerId") Long ledgerId);
}

