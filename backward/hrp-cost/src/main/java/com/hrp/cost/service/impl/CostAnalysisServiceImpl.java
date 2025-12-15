package com.hrp.cost.service.impl;

import com.hrp.cost.mapper.CostAnalysisMapper;
import com.hrp.cost.service.CostAnalysisService;
import com.hrp.common.entity.CostAnalysis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CostAnalysisServiceImpl implements CostAnalysisService {

    @Autowired
    private CostAnalysisMapper costAnalysisMapper;

    @Override
    public List<CostAnalysis> getAll() {
        return costAnalysisMapper.selectAll();
    }

    @Override
    public CostAnalysis getById(Long id) {
        return costAnalysisMapper.selectById(id);
    }

    @Override
    @Transactional
    public boolean save(CostAnalysis costAnalysis) {
        if (costAnalysis.getAnalysisNo() == null || costAnalysis.getAnalysisNo().isEmpty()) {
            costAnalysis.setAnalysisNo("ANALYSIS" + System.currentTimeMillis());
        }
        return costAnalysisMapper.insert(costAnalysis) > 0;
    }

    @Override
    @Transactional
    public boolean update(CostAnalysis costAnalysis) {
        return costAnalysisMapper.updateById(costAnalysis) > 0;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        return costAnalysisMapper.deleteById(id) > 0;
    }

    @Override
    public Map<String, Object> getDetail(Long id) {
        CostAnalysis analysis = costAnalysisMapper.selectById(id);
        if (analysis == null) {
            return null;
        }
        
        Map<String, Object> detail = new HashMap<>();
        detail.put("analysis", analysis);
        
        // 查询成本明细数据
        // TODO: 根据实际业务需求，从成本明细表查询数据
        List<Map<String, Object>> details = new ArrayList<>();
        
        // 示例数据结构
        if (analysis.getTotalCost() != null) {
            Map<String, Object> item1 = new HashMap<>();
            item1.put("costItem", "人工成本");
            item1.put("costItemCode", "LABOR");
            item1.put("costAmount", analysis.getDirectCost() != null ? analysis.getDirectCost().multiply(new BigDecimal("0.6")) : BigDecimal.ZERO);
            item1.put("costRatio", analysis.getCostRatio() != null ? analysis.getCostRatio().multiply(new BigDecimal("0.6")) : BigDecimal.ZERO);
            item1.put("lastPeriodAmount", BigDecimal.ZERO);
            item1.put("growthRate", analysis.getGrowthRate() != null ? analysis.getGrowthRate() : BigDecimal.ZERO);
            details.add(item1);
            
            Map<String, Object> item2 = new HashMap<>();
            item2.put("costItem", "材料成本");
            item2.put("costItemCode", "MATERIAL");
            item2.put("costAmount", analysis.getDirectCost() != null ? analysis.getDirectCost().multiply(new BigDecimal("0.4")) : BigDecimal.ZERO);
            item2.put("costRatio", analysis.getCostRatio() != null ? analysis.getCostRatio().multiply(new BigDecimal("0.4")) : BigDecimal.ZERO);
            item2.put("lastPeriodAmount", BigDecimal.ZERO);
            item2.put("growthRate", analysis.getGrowthRate() != null ? analysis.getGrowthRate() : BigDecimal.ZERO);
            details.add(item2);
        }
        
        detail.put("details", details);
        
        return detail;
    }

    @Override
    public List<Map<String, Object>> compare(String period1, String period2) {
        List<CostAnalysis> list1 = costAnalysisMapper.selectByPeriod(period1);
        List<CostAnalysis> list2 = costAnalysisMapper.selectByPeriod(period2);
        
        // 实现成本对比逻辑
        List<Map<String, Object>> compareData = new ArrayList<>();
        
        // 汇总两个期间的成本数据
        BigDecimal total1 = list1.stream()
            .map(a -> a.getTotalCost() != null ? a.getTotalCost() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal total2 = list2.stream()
            .map(a -> a.getTotalCost() != null ? a.getTotalCost() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal direct1 = list1.stream()
            .map(a -> a.getDirectCost() != null ? a.getDirectCost() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal direct2 = list2.stream()
            .map(a -> a.getDirectCost() != null ? a.getDirectCost() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal indirect1 = list1.stream()
            .map(a -> a.getIndirectCost() != null ? a.getIndirectCost() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal indirect2 = list2.stream()
            .map(a -> a.getIndirectCost() != null ? a.getIndirectCost() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 构建对比数据
        Map<String, Object> item1 = new HashMap<>();
        item1.put("costItem", "直接成本");
        item1.put("amount1", direct1);
        item1.put("amount2", direct2);
        item1.put("diff", direct2.subtract(direct1));
        item1.put("changeRate", direct1.compareTo(BigDecimal.ZERO) > 0 
            ? direct2.subtract(direct1).divide(direct1, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
            : BigDecimal.ZERO);
        compareData.add(item1);
        
        Map<String, Object> item2 = new HashMap<>();
        item2.put("costItem", "间接成本");
        item2.put("amount1", indirect1);
        item2.put("amount2", indirect2);
        item2.put("diff", indirect2.subtract(indirect1));
        item2.put("changeRate", indirect1.compareTo(BigDecimal.ZERO) > 0 
            ? indirect2.subtract(indirect1).divide(indirect1, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
            : BigDecimal.ZERO);
        compareData.add(item2);
        
        Map<String, Object> item3 = new HashMap<>();
        item3.put("costItem", "总成本");
        item3.put("amount1", total1);
        item3.put("amount2", total2);
        item3.put("diff", total2.subtract(total1));
        item3.put("changeRate", total1.compareTo(BigDecimal.ZERO) > 0 
            ? total2.subtract(total1).divide(total1, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
            : BigDecimal.ZERO);
        compareData.add(item3);
        
        return compareData;
    }
}

