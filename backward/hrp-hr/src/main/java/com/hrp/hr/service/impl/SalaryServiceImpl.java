package com.hrp.hr.service.impl;

import com.hrp.hr.mapper.SalaryMapper;
import com.hrp.hr.service.SalaryService;
import com.hrp.common.entity.Salary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SalaryServiceImpl implements SalaryService {

    @Autowired
    private SalaryMapper salaryMapper;

    @Override
    public List<Salary> getAll() {
        return salaryMapper.selectAll();
    }

    @Override
    public List<Salary> getByEmpId(Long empId) {
        return salaryMapper.selectByEmpId(empId);
    }

    @Override
    public List<Salary> getByMonth(String salaryMonth) {
        return salaryMapper.selectByMonth(salaryMonth);
    }

    @Override
    public Salary getById(Long id) {
        return salaryMapper.selectById(id);
    }

    @Override
    public Salary getByEmpIdAndMonth(Long empId, String salaryMonth) {
        return salaryMapper.selectByEmpIdAndMonth(empId, salaryMonth);
    }

    @Override
    @Transactional
    public boolean save(Salary salary) {
        if (salary.getStatus() == null || salary.getStatus().isEmpty()) {
            salary.setStatus("PENDING");
        }
        calculateTotal(salary);
        return salaryMapper.insert(salary) > 0;
    }

    @Override
    @Transactional
    public boolean update(Salary salary) {
        calculateTotal(salary);
        return salaryMapper.updateById(salary) > 0;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        return salaryMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional
    public boolean calculateSalary(Long empId, String salaryMonth) {
        Salary salary = salaryMapper.selectByEmpIdAndMonth(empId, salaryMonth);
        if (salary == null) {
            return false;
        }
        calculateTotal(salary);
        salary.setStatus("CALCULATED");
        return salaryMapper.updateById(salary) > 0;
    }

    private void calculateTotal(Salary salary) {
        BigDecimal total = BigDecimal.ZERO;
        if (salary.getBaseSalary() != null) {
            total = total.add(salary.getBaseSalary());
        }
        if (salary.getPerformanceSalary() != null) {
            total = total.add(salary.getPerformanceSalary());
        }
        if (salary.getAllowance() != null) {
            total = total.add(salary.getAllowance());
        }
        if (salary.getBonus() != null) {
            total = total.add(salary.getBonus());
        }
        if (salary.getDeduction() != null) {
            total = total.subtract(salary.getDeduction());
        }
        salary.setTotalSalary(total);

        // 计算个人所得税（简化计算，实际应按照税法计算）
        BigDecimal tax = BigDecimal.ZERO;
        if (total.compareTo(new BigDecimal("5000")) > 0) {
            tax = total.subtract(new BigDecimal("5000")).multiply(new BigDecimal("0.1"));
        }
        salary.setTax(tax);

        // 实发工资 = 应发工资 - 个人所得税
        salary.setActualSalary(total.subtract(tax));
    }
}

