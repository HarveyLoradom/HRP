package com.hrp.cost.service.impl;

import com.hrp.cost.mapper.CostReportMapper;
import com.hrp.cost.service.CostReportService;
import com.hrp.common.entity.CostReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CostReportServiceImpl implements CostReportService {

    @Autowired
    private CostReportMapper costReportMapper;

    @Override
    public List<CostReport> getAll() {
        return costReportMapper.selectAll();
    }

    @Override
    public List<CostReport> getByPeriod(String reportPeriod) {
        return costReportMapper.selectByPeriod(reportPeriod);
    }

    @Override
    public CostReport getById(Long id) {
        return costReportMapper.selectById(id);
    }

    @Override
    @Transactional
    public boolean save(CostReport costReport) {
        if (costReport.getReportNo() == null || costReport.getReportNo().isEmpty()) {
            costReport.setReportNo("COST" + System.currentTimeMillis());
        }
        return costReportMapper.insert(costReport) > 0;
    }

    @Override
    @Transactional
    public boolean update(CostReport costReport) {
        return costReportMapper.updateById(costReport) > 0;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        return costReportMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional
    public CostReport generate(Map<String, Object> params) {
        CostReport report = new CostReport();
        String reportType = (String) params.get("reportType");
        String reportPeriod = (String) params.get("reportPeriod");
        String reportName = (String) params.get("reportName");
        
        if (reportName == null || reportName.isEmpty()) {
            String typeName = "月度报表";
            if ("QUARTERLY".equals(reportType)) {
                typeName = "季度报表";
            } else if ("YEARLY".equals(reportType)) {
                typeName = "年度报表";
            }
            reportName = typeName + "-" + reportPeriod;
        }
        
        report.setReportNo("COST-" + System.currentTimeMillis());
        report.setReportName(reportName);
        report.setReportType(reportType);
        report.setReportPeriod(reportPeriod);
        report.setCreateTime(LocalDateTime.now());
        
        // 根据期间计算实际成本数据
        // TODO: 从成本核算表或成本明细表查询数据并汇总
        // 这里使用示例逻辑
        BigDecimal directCost = BigDecimal.ZERO;
        BigDecimal indirectCost = BigDecimal.ZERO;
        
        // 根据报表类型和期间查询成本数据
        if (reportPeriod != null && !reportPeriod.isEmpty()) {
            // 示例：查询该期间的成本核算数据
            List<CostReport> existingReports = costReportMapper.selectByPeriod(reportPeriod);
            
            if (!existingReports.isEmpty()) {
                // 如果已有报表，汇总数据
                directCost = existingReports.stream()
                    .map(r -> r.getDirectCost() != null ? r.getDirectCost() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                indirectCost = existingReports.stream()
                    .map(r -> r.getIndirectCost() != null ? r.getIndirectCost() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            } else {
                // 新报表，从成本核算数据计算
                // TODO: 从成本核算表查询该期间的数据并汇总
                directCost = new BigDecimal("200000.0"); // 示例数据
                indirectCost = new BigDecimal("50000.0"); // 示例数据
            }
        }
        
        report.setDirectCost(directCost);
        report.setIndirectCost(indirectCost);
        report.setTotalCost(directCost.add(indirectCost));
        
        if (costReportMapper.insert(report) > 0) {
            return report;
        }
        return null;
    }

    @Override
    public Map<String, Object> getChartData(Long reportId) {
        CostReport report = costReportMapper.selectById(reportId);
        if (report == null) {
            return null;
        }
        
        Map<String, Object> chartData = new HashMap<>();
        chartData.put("report", report);
        chartData.put("pieData", List.of(
            Map.of("name", "直接成本", "value", report.getDirectCost() != null ? report.getDirectCost() : BigDecimal.ZERO),
            Map.of("name", "间接成本", "value", report.getIndirectCost() != null ? report.getIndirectCost() : BigDecimal.ZERO)
        ));
        chartData.put("barData", List.of(
            report.getTotalCost() != null ? report.getTotalCost() : BigDecimal.ZERO,
            report.getDirectCost() != null ? report.getDirectCost() : BigDecimal.ZERO,
            report.getIndirectCost() != null ? report.getIndirectCost() : BigDecimal.ZERO
        ));
        
        return chartData;
    }
}

