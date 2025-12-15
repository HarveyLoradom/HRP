package com.hrp.hr.mapper.impl;

import com.hrp.hr.mapper.PerformanceMapper;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class PerformanceMapperImpl implements PerformanceMapper {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    private static final String NAMESPACE = "com.hrp.hr.mapper.PerformanceMapper";

    @Override
    public List<?> selectByEmpIdAndPeriod(Map<String, Object> params) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByEmpIdAndPeriod", params);
    }
}





