package com.hrp.efficiency.service.impl;

import com.hrp.efficiency.service.InvestmentService;
import com.hrp.efficiency.mapper.InvestmentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InvestmentServiceImpl implements InvestmentService {

    @Autowired
    private InvestmentMapper investmentMapper;

    @Override
    public List<?> getAll() {
        return investmentMapper.selectAll();
    }

    @Override
    public Object getById(Long id) {
        return investmentMapper.selectById(id);
    }

    @Override
    @Transactional
    public boolean save(Map<String, Object> data) {
        if (data.get("analysisId") == null) {
            // 新增
            data.put("analysisNo", "INV-" + System.currentTimeMillis());
            data.put("createTime", LocalDateTime.now());
            return investmentMapper.insert(data) > 0;
        } else {
            // 更新
            data.put("updateTime", LocalDateTime.now());
            return investmentMapper.updateById(data) > 0;
        }
    }

    @Override
    public Map<String, Object> calculate(Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            BigDecimal investmentAmount = new BigDecimal(data.get("investmentAmount").toString());
            BigDecimal returnAmount = new BigDecimal(data.get("returnAmount").toString());
            
            if (investmentAmount.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal returnRate = returnAmount.subtract(investmentAmount)
                    .divide(investmentAmount, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(new BigDecimal("100"));
                
                result.put("investmentAmount", investmentAmount);
                result.put("returnAmount", returnAmount);
                result.put("profit", returnAmount.subtract(investmentAmount));
                result.put("returnRate", returnRate);
            } else {
                result.put("error", "投资金额必须大于0");
            }
        } catch (Exception e) {
            result.put("error", "计算失败：" + e.getMessage());
        }
        
        return result;
    }
}





