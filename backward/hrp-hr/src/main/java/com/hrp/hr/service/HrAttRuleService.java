package com.hrp.hr.service;

import com.hrp.common.entity.HrAttRule;
import java.util.List;

/**
 * 考勤规则配置服务接口
 */
public interface HrAttRuleService {
    /**
     * 根据ID查询
     */
    HrAttRule getById(Integer ruleId);
    
    /**
     * 根据规则类型查询
     */
    List<HrAttRule> getByRuleType(String ruleType);
    
    /**
     * 查询所有规则
     */
    List<HrAttRule> getAll();
    
    /**
     * 保存规则
     */
    HrAttRule save(HrAttRule rule);
    
    /**
     * 更新规则
     */
    HrAttRule update(HrAttRule rule);
    
    /**
     * 删除规则
     */
    boolean delete(Integer ruleId);
}

