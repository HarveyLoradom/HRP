package com.hrp.hr.service.impl;

import com.hrp.common.entity.HrAttRule;
import com.hrp.common.exception.BusinessException;
import com.hrp.hr.mapper.HrAttRuleMapper;
import com.hrp.hr.service.HrAttRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HrAttRuleServiceImpl implements HrAttRuleService {

    @Autowired
    private HrAttRuleMapper hrAttRuleMapper;

    @Override
    public HrAttRule getById(Integer ruleId) {
        return hrAttRuleMapper.selectById(ruleId);
    }

    @Override
    public List<HrAttRule> getByRuleType(String ruleType) {
        return hrAttRuleMapper.selectByRuleType(ruleType);
    }

    @Override
    public List<HrAttRule> getAll() {
        return hrAttRuleMapper.selectAll();
    }

    @Override
    @Transactional
    public HrAttRule save(HrAttRule rule) {
        if (rule.getRuleType() == null) {
            throw new BusinessException("规则类型不能为空");
        }
        if (rule.getRuleName() == null || rule.getRuleName().trim().isEmpty()) {
            throw new BusinessException("规则名称不能为空");
        }
        if (rule.getSalCoefficient() == null) {
            throw new BusinessException("薪酬影响系数不能为空");
        }
        hrAttRuleMapper.insert(rule);
        return rule;
    }

    @Override
    @Transactional
    public HrAttRule update(HrAttRule rule) {
        if (rule.getRuleId() == null) {
            throw new BusinessException("规则ID不能为空");
        }
        hrAttRuleMapper.updateById(rule);
        return hrAttRuleMapper.selectById(rule.getRuleId());
    }

    @Override
    @Transactional
    public boolean delete(Integer ruleId) {
        return hrAttRuleMapper.deleteById(ruleId) > 0;
    }
}

