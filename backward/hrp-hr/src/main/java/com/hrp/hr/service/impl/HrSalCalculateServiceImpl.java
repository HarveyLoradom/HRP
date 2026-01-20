package com.hrp.hr.service.impl;

import com.hrp.common.entity.*;
import com.hrp.common.exception.BusinessException;
import com.hrp.hr.mapper.*;
import com.hrp.hr.service.HrSalCalculateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class HrSalCalculateServiceImpl implements HrSalCalculateService {

    @Autowired
    private HrSalCalculateMapper hrSalCalculateMapper;
    
    @Autowired
    private HrSalConfigMapper hrSalConfigMapper;
    
    @Autowired
    private HrAttLedgerMapper hrAttLedgerMapper;
    
    @Autowired
    private HrAttRuleMapper hrAttRuleMapper;

    @Override
    public HrSalCalculate getById(Long calcId) {
        return hrSalCalculateMapper.selectById(calcId);
    }

    @Override
    public HrSalCalculate getByEmpIdAndMonth(Long empId, String calcMonth) {
        return hrSalCalculateMapper.selectByEmpIdAndMonth(empId, calcMonth);
    }

    @Override
    public com.hrp.common.entity.PageResult<HrSalCalculate> getPage(Long page, Long size,
                                                                     Long empId, String empCode, String empName,
                                                                     String calcMonth, String calcStatus) {
        List<HrSalCalculate> allList = hrSalCalculateMapper.selectByConditions(empId, empCode, empName, calcMonth, calcStatus);
        Long total = (long) allList.size();
        
        int start = (int) ((page - 1) * size);
        int end = Math.min(start + size.intValue(), allList.size());
        List<HrSalCalculate> list = allList.subList(start, end);
        
        return new com.hrp.common.entity.PageResult<>(list, total, page, size);
    }

    @Override
    @Transactional
    public HrSalCalculate calculateSalary(Long empId, String calcMonth) {
        // 1. 获取薪酬配置
        HrSalConfig config = hrSalConfigMapper.selectByEmpId(empId);
        if (config == null) {
            throw new BusinessException("该员工未配置薪酬信息");
        }

        // 2. 获取考勤台账
        HrAttLedger ledger = hrAttLedgerMapper.selectByEmpIdAndMonth(empId, calcMonth);
        if (ledger == null) {
            throw new BusinessException("该员工在" + calcMonth + "月份的考勤台账不存在，请先统计考勤");
        }
        if (ledger.getLedgerStatus() == null || !"UNCALC".equals(ledger.getLedgerStatus())) {
            throw new BusinessException("该员工在" + calcMonth + "月份的考勤台账状态不正确，只有未核算状态才能计算薪酬");
        }

        // 3. 获取考勤规则
        List<HrAttRule> rules = hrAttRuleMapper.selectAll();

        // 4. 计算各项工资
        BigDecimal basicSalary = config.getBasicSalary();
        BigDecimal postAllowance = config.getPostAllowance() != null ? config.getPostAllowance() : BigDecimal.ZERO;

        // 计算日薪 = 基本工资 / 21.75
        BigDecimal dailySalary = basicSalary.divide(BigDecimal.valueOf(21.75), 2, RoundingMode.HALF_UP);

        // 获取薪酬规则
        List<HrAttRule> absentRules = hrAttRuleMapper.selectByRuleType("ABSENT_DEDUCT"); // 旷工扣罚规则
        List<HrAttRule> overtimeRules = hrAttRuleMapper.selectByRuleType("OVERTIME_BONUS"); // 加班加薪规则
        List<HrAttRule> leaveRules = hrAttRuleMapper.selectByRuleType("LEAVE_DEDUCT"); // 请假扣薪规则

        // 旷工扣罚计算 = 日薪 * 旷工天数 * 旷工扣罚规则系数
        BigDecimal absentDeduct = BigDecimal.ZERO;
        if (ledger.getAbsentDays() != null && ledger.getAbsentDays().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal absentCoefficient = BigDecimal.ONE; // 默认系数为1（不扣）
            if (!absentRules.isEmpty()) {
                // 使用第一个旷工规则（如果有多个，可以使用更复杂的逻辑）
                absentCoefficient = absentRules.get(0).getSalCoefficient();
            }
            absentDeduct = dailySalary.multiply(ledger.getAbsentDays()).multiply(absentCoefficient);
        }

        // 加班工资计算 = 日薪 * 加班天数 * 加班加薪规则系数
        BigDecimal overtimeSalary = BigDecimal.ZERO;
        if (ledger.getOvertimeDays() != null && ledger.getOvertimeDays().doubleValue() > 0) {
            BigDecimal overtimeCoefficient = BigDecimal.valueOf(1.5); // 默认1.5倍
            if (!overtimeRules.isEmpty()) {
                overtimeCoefficient = overtimeRules.get(0).getSalCoefficient();
            }
            overtimeSalary = dailySalary.multiply(ledger.getOvertimeDays()).multiply(overtimeCoefficient);
        }

        // 请假扣薪计算 = 日薪 * 请假天数 * (1 - 请假规则系数)
        BigDecimal leaveDeduct = BigDecimal.ZERO;
        if (ledger.getLeaveDays() != null && ledger.getLeaveDays().doubleValue() > 0) {
            BigDecimal leaveCoefficient = BigDecimal.valueOf(0.8); // 默认发80%，扣20%
            if (!leaveRules.isEmpty()) {
                leaveCoefficient = leaveRules.get(0).getSalCoefficient();
            }
            // 扣薪 = 日薪 * 请假天数 * (1 - 系数)，例如系数0.8表示发80%，扣20%
            leaveDeduct = dailySalary.multiply(ledger.getLeaveDays()).multiply(BigDecimal.ONE.subtract(leaveCoefficient));
        }

        // 应发工资 = 基本工资 + 岗位津贴 + 加班工资 - 请假扣薪 - 旷工扣罚
        BigDecimal totalIncome = basicSalary.add(postAllowance).add(overtimeSalary)
                .subtract(leaveDeduct).subtract(absentDeduct);

        // 5. 计算代扣项
        BigDecimal socialSecurity = BigDecimal.ZERO;
        BigDecimal providentFund = BigDecimal.ZERO;
        if (config.getSocialSecurity() != null && config.getSocialSecurity().doubleValue() > 0) {
            socialSecurity = totalIncome.multiply(config.getSocialSecurity()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        if (config.getProvidentFund() != null && config.getProvidentFund().doubleValue() > 0) {
            providentFund = totalIncome.multiply(config.getProvidentFund()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        // 6. 计算个人所得税（2026年税率）
        // 应纳税所得额 = 应发工资 - 社保 - 公积金 - 起征点
        BigDecimal taxThreshold = config.getTaxThreshold() != null ? config.getTaxThreshold() : BigDecimal.valueOf(5000);
        BigDecimal taxableIncome = totalIncome.subtract(socialSecurity).subtract(providentFund).subtract(taxThreshold);
        BigDecimal personalTax = calculatePersonalTax2026(taxableIncome);

        // 7. 实发工资 = 应发工资 - 社保 - 公积金 - 个人所得税
        BigDecimal netSalary = totalIncome.subtract(socialSecurity).subtract(providentFund).subtract(personalTax);

        // 8. 创建或更新薪酬核算记录
        HrSalCalculate calculate = hrSalCalculateMapper.selectByEmpIdAndMonth(empId, calcMonth);
        if (calculate == null) {
            calculate = new HrSalCalculate();
            calculate.setEmpId(empId);
            calculate.setCalcMonth(calcMonth);
        }

        calculate.setBasicSalary(basicSalary);
        calculate.setPostAllowance(postAllowance);
        calculate.setOvertimeSalary(overtimeSalary);
        calculate.setLeaveDeduct(leaveDeduct);
        calculate.setAbsentDeduct(absentDeduct);
        calculate.setTotalIncome(totalIncome);
        calculate.setSocialSecurity(socialSecurity);
        calculate.setProvidentFund(providentFund);
        calculate.setPersonalTax(personalTax);
        calculate.setNetSalary(netSalary);
        calculate.setCalcStatus("CALC"); // 已核算

        if (calculate.getCalcId() == null) {
            hrSalCalculateMapper.insert(calculate);
        } else {
            hrSalCalculateMapper.updateById(calculate);
        }

        // 9. 计算完薪酬后，更新考勤台账状态为"已核算"（CALC）
        ledger.setLedgerStatus("CALC");
        hrAttLedgerMapper.updateById(ledger);

        return calculate;
    }

    /**
     * 计算个人所得税（2026年税率）
     * 2026年个人所得税累进税率表（7级）
     */
    private BigDecimal calculatePersonalTax2026(BigDecimal taxableIncome) {
        if (taxableIncome.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // 2026年个人所得税累进税率表
        // 不超过3000元的，税率3%，速算扣除数0
        if (taxableIncome.compareTo(BigDecimal.valueOf(3000)) <= 0) {
            return taxableIncome.multiply(BigDecimal.valueOf(0.03)).setScale(2, RoundingMode.HALF_UP);
        }
        // 超过3000元至12000元的部分，税率10%，速算扣除数210
        else if (taxableIncome.compareTo(BigDecimal.valueOf(12000)) <= 0) {
            return taxableIncome.multiply(BigDecimal.valueOf(0.1)).subtract(BigDecimal.valueOf(210)).setScale(2, RoundingMode.HALF_UP);
        }
        // 超过12000元至25000元的部分，税率20%，速算扣除数1410
        else if (taxableIncome.compareTo(BigDecimal.valueOf(25000)) <= 0) {
            return taxableIncome.multiply(BigDecimal.valueOf(0.2)).subtract(BigDecimal.valueOf(1410)).setScale(2, RoundingMode.HALF_UP);
        }
        // 超过25000元至35000元的部分，税率25%，速算扣除数2660
        else if (taxableIncome.compareTo(BigDecimal.valueOf(35000)) <= 0) {
            return taxableIncome.multiply(BigDecimal.valueOf(0.25)).subtract(BigDecimal.valueOf(2660)).setScale(2, RoundingMode.HALF_UP);
        }
        // 超过35000元至55000元的部分，税率30%，速算扣除数4410
        else if (taxableIncome.compareTo(BigDecimal.valueOf(55000)) <= 0) {
            return taxableIncome.multiply(BigDecimal.valueOf(0.3)).subtract(BigDecimal.valueOf(4410)).setScale(2, RoundingMode.HALF_UP);
        }
        // 超过55000元至80000元的部分，税率35%，速算扣除数7160
        else if (taxableIncome.compareTo(BigDecimal.valueOf(80000)) <= 0) {
            return taxableIncome.multiply(BigDecimal.valueOf(0.35)).subtract(BigDecimal.valueOf(7160)).setScale(2, RoundingMode.HALF_UP);
        }
        // 超过80000元的部分，税率45%，速算扣除数15160
        else {
            return taxableIncome.multiply(BigDecimal.valueOf(0.45)).subtract(BigDecimal.valueOf(15160)).setScale(2, RoundingMode.HALF_UP);
        }
    }

    @Override
    @Transactional
    public List<HrSalCalculate> batchCalculateSalary(String calcMonth, List<Long> empIds) {
        List<HrSalCalculate> results = new ArrayList<>();
        for (Long empId : empIds) {
            try {
                HrSalCalculate calculate = calculateSalary(empId, calcMonth);
                results.add(calculate);
            } catch (Exception e) {
                // 失败时抛出异常，由调用方处理
                throw new BusinessException("员工ID " + empId + " 薪酬计算失败：" + e.getMessage());
            }
        }
        return results;
    }

    @Override
    @Transactional
    public HrSalCalculate update(HrSalCalculate calculate) {
        if (calculate.getCalcId() == null) {
            throw new BusinessException("核算ID不能为空");
        }
        hrSalCalculateMapper.updateById(calculate);
        return hrSalCalculateMapper.selectById(calculate.getCalcId());
    }

    @Override
    @Transactional
    public boolean delete(Long calcId) {
        return hrSalCalculateMapper.deleteById(calcId) > 0;
    }

    @Override
    @Transactional
    public boolean paySalary(Long calcId) {
        HrSalCalculate calculate = hrSalCalculateMapper.selectById(calcId);
        if (calculate == null) {
            throw new BusinessException("薪酬核算记录不存在");
        }
        if (!"CALC".equals(calculate.getCalcStatus())) {
            throw new BusinessException("只有已核算状态的薪酬才能发放");
        }
        calculate.setCalcStatus("PAID"); // 已发放
        return hrSalCalculateMapper.updateById(calculate) > 0;
    }

    @Override
    @Transactional
    public boolean batchPaySalary(List<Long> calcIds) {
        if (calcIds == null || calcIds.isEmpty()) {
            return false;
        }
        for (Long calcId : calcIds) {
            paySalary(calcId);
        }
        return true;
    }
}
