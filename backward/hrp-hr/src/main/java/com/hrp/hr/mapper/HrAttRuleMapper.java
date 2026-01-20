package com.hrp.hr.mapper;

import com.hrp.common.entity.HrAttRule;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 考勤规则配置表数据访问接口
 */
public interface HrAttRuleMapper {
    HrAttRule selectById(@Param("ruleId") Integer ruleId);
    List<HrAttRule> selectByRuleType(@Param("ruleType") String ruleType);
    List<HrAttRule> selectAll();
    int insert(HrAttRule rule);
    int updateById(HrAttRule rule);
    int deleteById(@Param("ruleId") Integer ruleId);
}

