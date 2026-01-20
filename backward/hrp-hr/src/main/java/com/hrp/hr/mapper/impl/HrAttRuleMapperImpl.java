package com.hrp.hr.mapper.impl;

import com.hrp.hr.mapper.HrAttRuleMapper;
import com.hrp.common.entity.HrAttRule;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 考勤规则配置表数据访问实现类
 */
@Repository
public class HrAttRuleMapperImpl implements HrAttRuleMapper {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    private static final String NAMESPACE = "com.hrp.hr.mapper.HrAttRuleMapper";

    @Override
    public HrAttRule selectById(Integer ruleId) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", ruleId);
    }

    @Override
    public List<HrAttRule> selectByRuleType(String ruleType) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByRuleType", ruleType);
    }

    @Override
    public List<HrAttRule> selectAll() {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAll");
    }

    @Override
    public int insert(HrAttRule rule) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", rule);
    }

    @Override
    public int updateById(HrAttRule rule) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", rule);
    }

    @Override
    public int deleteById(Integer ruleId) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", ruleId);
    }
}

