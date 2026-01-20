package com.hrp.hr.service;

import com.hrp.common.entity.HrAttLedger;
import java.util.List;

/**
 * 员工考勤台账服务接口
 */
public interface HrAttLedgerService {
    /**
     * 根据ID查询
     */
    HrAttLedger getById(Long ledgerId);
    
    /**
     * 根据员工ID和月份查询
     */
    HrAttLedger getByEmpIdAndMonth(Long empId, String attMonth);
    
    /**
     * 分页查询
     */
    com.hrp.common.entity.PageResult<HrAttLedger> getPage(Long page, Long size, Long empId,
                                                          String attMonth, String ledgerStatus,
                                                          String empCode, String empName, Long deptId);

    /**
     * 统计考勤（按考勤月份 + 周期计算全员台账）
     * @return 写入/更新的台账数量
     */
    int calculateAll(String attMonth, String startDate, String endDate, Integer monthWorkDays);

    /**
     * 批量导入考勤台账
     * @param dataList Excel数据列表（第一行为表头，从第二行开始为数据）
     * @param createUser 创建人
     * @return 导入结果
     */
    com.hrp.common.entity.Result<String> importLedgers(List<List<String>> dataList, String createUser);
}
