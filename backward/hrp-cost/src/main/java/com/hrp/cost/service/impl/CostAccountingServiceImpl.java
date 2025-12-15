package com.hrp.cost.service.impl;

import com.hrp.cost.mapper.CostAccountingMapper;
import com.hrp.cost.service.CostAccountingService;
import com.hrp.common.entity.CostAccounting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CostAccountingServiceImpl implements CostAccountingService {

    @Autowired
    private CostAccountingMapper costAccountingMapper;

    @Override
    public List<CostAccounting> getAll() {
        return costAccountingMapper.selectAll();
    }

    @Override
    public List<CostAccounting> getByPeriod(String accountingPeriod) {
        return costAccountingMapper.selectByPeriod(accountingPeriod);
    }

    @Override
    public CostAccounting getById(Long id) {
        return costAccountingMapper.selectById(id);
    }

    @Override
    @Transactional
    public boolean save(CostAccounting costAccounting) {
        if (costAccounting.getAccountingNo() == null || costAccounting.getAccountingNo().isEmpty()) {
            costAccounting.setAccountingNo("ACCOUNTING" + System.currentTimeMillis());
        }
        return costAccountingMapper.insert(costAccounting) > 0;
    }

    @Override
    @Transactional
    public boolean update(CostAccounting costAccounting) {
        return costAccountingMapper.updateById(costAccounting) > 0;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        return costAccountingMapper.deleteById(id) > 0;
    }

    @Override
    public Map<String, Object> getDetail(Long id) {
        CostAccounting accounting = costAccountingMapper.selectById(id);
        if (accounting == null) {
            return null;
        }
        
        Map<String, Object> detail = new HashMap<>();
        detail.put("accounting", accounting);
        
        // 查询成本明细数据
        // TODO: 根据实际业务需求，从成本明细表查询数据
        List<Map<String, Object>> details = new ArrayList<>();
        
        // 示例数据结构
        if (accounting.getCostAmount() != null && accounting.getCostAmount().compareTo(BigDecimal.ZERO) > 0) {
            Map<String, Object> item1 = new HashMap<>();
            item1.put("costItemCode", "LABOR_001");
            item1.put("costItemName", "人工成本");
            item1.put("costType", "DIRECT");
            item1.put("costAmount", accounting.getDirectCost() != null ? accounting.getDirectCost() : BigDecimal.ZERO);
            item1.put("allocationMethod", "PROPORTION");
            item1.put("allocationBase", "按人数");
            item1.put("remark", "");
            details.add(item1);
            
            Map<String, Object> item2 = new HashMap<>();
            item2.put("costItemCode", "MATERIAL_001");
            item2.put("costItemName", "材料成本");
            item2.put("costType", "DIRECT");
            item2.put("costAmount", accounting.getDirectCost() != null ? accounting.getDirectCost().multiply(new BigDecimal("0.5")) : BigDecimal.ZERO);
            item2.put("allocationMethod", "PROPORTION");
            item2.put("allocationBase", "按比例");
            item2.put("remark", "");
            details.add(item2);
            
            Map<String, Object> item3 = new HashMap<>();
            item3.put("costItemCode", "OVERHEAD_001");
            item3.put("costItemName", "管理费用");
            item3.put("costType", "INDIRECT");
            item3.put("costAmount", accounting.getIndirectCost() != null ? accounting.getIndirectCost() : BigDecimal.ZERO);
            item3.put("allocationMethod", "AVERAGE");
            item3.put("allocationBase", "平均分摊");
            item3.put("remark", "");
            details.add(item3);
        }
        
        detail.put("details", details);
        
        return detail;
    }

    @Override
    @Transactional
    public boolean calculate(Long accountingId) {
        CostAccounting accounting = costAccountingMapper.selectById(accountingId);
        if (accounting == null || !"DRAFT".equals(accounting.getStatus())) {
            return false;
        }
        
        // 实现成本核算逻辑，计算直接成本和间接成本
        // TODO: 根据实际业务需求，从数据库查询成本数据并计算
        // 这里使用示例逻辑
        BigDecimal directCost = BigDecimal.ZERO;
        BigDecimal indirectCost = BigDecimal.ZERO;
        
        // 示例：根据核算期间和维度查询成本数据
        // 直接成本包括：人工成本、材料成本等
        // 间接成本包括：管理费用、折旧等
        
        // 这里只是示例计算，实际需要从数据库查询
        if (accounting.getAccountingDimension() != null) {
            switch (accounting.getAccountingDimension()) {
                case "DEPT":
                    // 科室维度核算
                    directCost = new BigDecimal("100000.0");
                    indirectCost = new BigDecimal("30000.0");
                    break;
                case "PROJECT":
                    // 项目维度核算
                    directCost = new BigDecimal("150000.0");
                    indirectCost = new BigDecimal("40000.0");
                    break;
                case "EQUIPMENT":
                    // 设备维度核算
                    directCost = new BigDecimal("80000.0");
                    indirectCost = new BigDecimal("20000.0");
                    break;
                default:
                    directCost = BigDecimal.ZERO;
                    indirectCost = BigDecimal.ZERO;
            }
        }
        
        accounting.setStatus("CALCULATED");
        accounting.setCostAmount(directCost.add(indirectCost));
        accounting.setDirectCost(directCost);
        accounting.setIndirectCost(indirectCost);
        
        return costAccountingMapper.updateById(accounting) > 0;
    }
}

