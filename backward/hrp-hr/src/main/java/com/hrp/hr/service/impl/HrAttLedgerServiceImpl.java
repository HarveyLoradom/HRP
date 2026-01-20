package com.hrp.hr.service.impl;

import com.hrp.common.entity.HrAttLedger;
import com.hrp.common.entity.PageResult;
import com.hrp.common.entity.Result;
import com.hrp.common.exception.BusinessException;
import com.hrp.hr.mapper.HrAttLedgerMapper;
import com.hrp.hr.mapper.HrAttRecordMapper;
import com.hrp.hr.mapper.SysEmpMapper;
import com.hrp.hr.service.HrAttLedgerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HrAttLedgerServiceImpl implements HrAttLedgerService {

    @Autowired
    private HrAttLedgerMapper hrAttLedgerMapper;

    @Autowired
    private SysEmpMapper sysEmpMapper;

    @Autowired
    private HrAttRecordMapper hrAttRecordMapper;

    @Override
    public HrAttLedger getById(Long ledgerId) {
        return hrAttLedgerMapper.selectById(ledgerId);
    }

    @Override
    public HrAttLedger getByEmpIdAndMonth(Long empId, String attMonth) {
        return hrAttLedgerMapper.selectByEmpIdAndMonth(empId, attMonth);
    }

    @Override
    public PageResult<HrAttLedger> getPage(Long page, Long size, Long empId,
                                           String attMonth, String ledgerStatus,
                                           String empCode, String empName, Long deptId) {
        List<HrAttLedger> allList = hrAttLedgerMapper.selectByConditions(empId, attMonth, ledgerStatus, empCode, empName, deptId);
        Long total = (long) allList.size();
        
        int start = (int) ((page - 1) * size);
        if (start >= allList.size()) {
            return new PageResult<>(java.util.Collections.emptyList(), total, size, page);
        }
        int end = Math.min(start + size.intValue(), allList.size());
        List<HrAttLedger> list = allList.subList(start, end);
        
        // PageResult(records, total, size, current)
        return new PageResult<>(list, total, size, page);
    }

    @Override
    public int calculateAll(String attMonth, String startDate, String endDate, Integer monthWorkDays) {
        if (attMonth == null || attMonth.trim().isEmpty()) {
            throw new BusinessException("考勤月份不能为空");
        }
        if (startDate == null || startDate.trim().isEmpty() || endDate == null || endDate.trim().isEmpty()) {
            throw new BusinessException("统计周期不能为空");
        }
        if (monthWorkDays == null) {
            throw new BusinessException("工作日不能为空（请手动输入）");
        }
        if (monthWorkDays < 0) {
            throw new BusinessException("工作日不能小于0");
        }

        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate start = LocalDate.parse(startDate, dateFmt);
        LocalDate end = LocalDate.parse(endDate, dateFmt);
        if (end.isBefore(start)) {
            throw new BusinessException("周期结束日期不能早于开始日期");
        }

        // 工作日由前端手动输入（按统计周期口径）
        int workDays = monthWorkDays;

        // 汇总：按员工聚合（正常/请假/加班）
        List<Map<String, Object>> summaries = hrAttRecordMapper.selectSummaryByRange(startDate, endDate);
        Map<Long, Map<String, BigDecimal>> summaryMap = new HashMap<>();
        for (Map<String, Object> row : summaries) {
            Long empId = row.get("empId") != null ? ((Number) row.get("empId")).longValue() : null;
            if (empId == null) continue;
            BigDecimal normal = toBigDecimal(row.get("normalDays"));
            BigDecimal leave = toBigDecimal(row.get("leaveDays"));
            BigDecimal overtime = toBigDecimal(row.get("overtimeDays"));
            Map<String, BigDecimal> m = new HashMap<>();
            m.put("normal", normal);
            m.put("leave", leave);
            m.put("overtime", overtime);
            summaryMap.put(empId, m);
        }

        List<Long> empIds = sysEmpMapper.selectActiveEmpIds();
        int affected = 0;
        for (Long empId : empIds) {
            Map<String, BigDecimal> s = summaryMap.get(empId);
            BigDecimal normalDays = s != null ? s.getOrDefault("normal", BigDecimal.ZERO) : BigDecimal.ZERO;
            BigDecimal leaveDays = s != null ? s.getOrDefault("leave", BigDecimal.ZERO) : BigDecimal.ZERO;
            BigDecimal overtimeDays = s != null ? s.getOrDefault("overtime", BigDecimal.ZERO) : BigDecimal.ZERO;

            BigDecimal workDaysBd = BigDecimal.valueOf(workDays);
            // 旷工天数 = 工作日 - 正常出勤 - 请假天数（不包含年假）
            BigDecimal absentDays = workDaysBd.subtract(normalDays).subtract(leaveDays);
            if (absentDays.compareTo(BigDecimal.ZERO) < 0) {
                absentDays = BigDecimal.ZERO;
            }

            HrAttLedger existing = hrAttLedgerMapper.selectByEmpIdAndMonth(empId, attMonth);
            if (existing == null) {
                HrAttLedger ledger = new HrAttLedger();
                ledger.setEmpId(empId);
                ledger.setAttMonth(attMonth);
                ledger.setMonthWorkDays(workDays);
                ledger.setAttDays(normalDays);
                ledger.setLeaveDays(leaveDays);
                ledger.setOvertimeDays(overtimeDays);
                ledger.setAbsentDays(absentDays);
                // 统计考勤后，状态设置为"未核算"（UNCALC），计算完薪酬后才更新为"已核算"（CALC）
                ledger.setLedgerStatus("UNCALC");
                hrAttLedgerMapper.insert(ledger);
            } else {
                existing.setMonthWorkDays(workDays);
                existing.setAttDays(normalDays);
                existing.setLeaveDays(leaveDays);
                existing.setOvertimeDays(overtimeDays);
                existing.setAbsentDays(absentDays);
                // 统计考勤后，状态设置为"未核算"（UNCALC），计算完薪酬后才更新为"已核算"（CALC）
                existing.setLedgerStatus("UNCALC");
                hrAttLedgerMapper.updateById(existing);
            }
            affected++;
        }

        return affected;
    }

    private BigDecimal toBigDecimal(Object val) {
        if (val == null) return BigDecimal.ZERO;
        if (val instanceof BigDecimal) return (BigDecimal) val;
        if (val instanceof Number) return BigDecimal.valueOf(((Number) val).doubleValue());
        try {
            return new BigDecimal(String.valueOf(val));
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    @Override
    @Transactional
    public Result<String> importLedgers(List<List<String>> dataList, String createUser) {
        if (dataList == null || dataList.isEmpty()) {
            return Result.error("导入数据为空");
        }

        int successCount = 0;
        int failCount = 0;
        StringBuilder errorMsg = new StringBuilder();

        // 跳过表头，从第二行开始
        for (int i = 1; i < dataList.size(); i++) {
            List<String> row = dataList.get(i);
            if (row == null || row.isEmpty()) {
                continue;
            }

            try {
                // Excel列顺序：员工编码、考勤月份、本月工作日、正常出勤天数、请假总天数、加班总天数、旷工天数（台账状态使用默认值，不导入）
                if (row.size() < 7) {
                    failCount++;
                    errorMsg.append("第").append(i + 1).append("行：数据列数不足，需要7列\n");
                    continue;
                }

                String empCode = row.get(0) != null ? row.get(0).trim() : "";
                String attMonth = row.get(1) != null ? row.get(1).trim() : "";
                String monthWorkDaysStr = row.get(2) != null ? row.get(2).trim() : "0";
                String attDaysStr = row.get(3) != null ? row.get(3).trim() : "0";
                String leaveDaysStr = row.get(4) != null ? row.get(4).trim() : "0";
                String overtimeDaysStr = row.get(5) != null ? row.get(5).trim() : "0";
                String absentDaysStr = row.get(6) != null ? row.get(6).trim() : "0";
                // 台账状态使用默认值，不导入

                // 验证必填字段
                if (empCode.isEmpty()) {
                    failCount++;
                    errorMsg.append("第").append(i + 1).append("行：员工编码不能为空\n");
                    continue;
                }
                if (attMonth.isEmpty()) {
                    failCount++;
                    errorMsg.append("第").append(i + 1).append("行：考勤月份不能为空\n");
                    continue;
                }

                // 根据员工编码查询员工ID
                Long empId = sysEmpMapper.selectEmpIdByCode(empCode);
                if (empId == null) {
                    failCount++;
                    errorMsg.append("第").append(i + 1).append("行：员工编码").append(empCode).append("不存在\n");
                    continue;
                }

                // 解析数字字段
                Integer monthWorkDays;
                BigDecimal attDays;
                BigDecimal leaveDays;
                BigDecimal overtimeDays;
                BigDecimal absentDays;

                try {
                    monthWorkDays = Integer.parseInt(monthWorkDaysStr);
                } catch (NumberFormatException e) {
                    failCount++;
                    errorMsg.append("第").append(i + 1).append("行：本月工作日格式错误\n");
                    continue;
                }

                try {
                    attDays = new BigDecimal(attDaysStr);
                } catch (NumberFormatException e) {
                    failCount++;
                    errorMsg.append("第").append(i + 1).append("行：正常出勤天数格式错误\n");
                    continue;
                }

                try {
                    leaveDays = new BigDecimal(leaveDaysStr);
                } catch (NumberFormatException e) {
                    failCount++;
                    errorMsg.append("第").append(i + 1).append("行：请假总天数格式错误\n");
                    continue;
                }

                try {
                    overtimeDays = new BigDecimal(overtimeDaysStr);
                } catch (NumberFormatException e) {
                    failCount++;
                    errorMsg.append("第").append(i + 1).append("行：加班总天数格式错误\n");
                    continue;
                }

                try {
                    absentDays = new BigDecimal(absentDaysStr);
                } catch (NumberFormatException e) {
                    failCount++;
                    errorMsg.append("第").append(i + 1).append("行：旷工天数格式错误\n");
                    continue;
                }

                // 验证考勤月份格式
                if (!attMonth.matches("\\d{4}-\\d{2}")) {
                    failCount++;
                    errorMsg.append("第").append(i + 1).append("行：考勤月份格式错误，应为yyyy-MM格式\n");
                    continue;
                }

                // 检查台账是否已存在
                HrAttLedger existing = hrAttLedgerMapper.selectByEmpIdAndMonth(empId, attMonth);
                if (existing != null) {
                    // 更新现有台账（台账状态保持不变，不更新）
                    existing.setMonthWorkDays(monthWorkDays);
                    existing.setAttDays(attDays);
                    existing.setLeaveDays(leaveDays);
                    existing.setOvertimeDays(overtimeDays);
                    existing.setAbsentDays(absentDays);
                    // 台账状态不更新，保持原值
                    existing.setUpdateTime(LocalDateTime.now());
                    int updated = hrAttLedgerMapper.updateById(existing);
                    if (updated > 0) {
                        successCount++;
                    } else {
                        failCount++;
                        errorMsg.append("第").append(i + 1).append("行：更新失败\n");
                    }
                } else {
                    // 创建新台账（台账状态使用默认值）
                    HrAttLedger ledger = new HrAttLedger();
                    ledger.setEmpId(empId);
                    ledger.setAttMonth(attMonth);
                    ledger.setMonthWorkDays(monthWorkDays);
                    ledger.setAttDays(attDays);
                    ledger.setLeaveDays(leaveDays);
                    ledger.setOvertimeDays(overtimeDays);
                    ledger.setAbsentDays(absentDays);
                    // 台账状态使用默认值
                    ledger.setLedgerStatus("UNCALC");
                    ledger.setCreateTime(LocalDateTime.now());
                    ledger.setUpdateTime(LocalDateTime.now());
                    int inserted = hrAttLedgerMapper.insert(ledger);
                    if (inserted > 0) {
                        successCount++;
                    } else {
                        failCount++;
                        errorMsg.append("第").append(i + 1).append("行：插入失败\n");
                    }
                }
            } catch (Exception e) {
                failCount++;
                errorMsg.append("第").append(i + 1).append("行：").append(e.getMessage()).append("\n");
            }
        }

        String message = String.format("导入完成：成功%d条，失败%d条", successCount, failCount);
        if (failCount > 0 && errorMsg.length() > 0) {
            message += "\n错误详情：\n" + errorMsg.toString();
        }

        if (failCount == 0) {
            return Result.success(message);
        } else if (successCount > 0) {
            return Result.error(message);
        } else {
            return Result.error(message);
        }
    }

}
