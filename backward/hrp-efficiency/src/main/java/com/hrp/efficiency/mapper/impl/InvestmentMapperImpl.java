package com.hrp.efficiency.mapper.impl;

import com.hrp.efficiency.mapper.InvestmentMapper;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class InvestmentMapperImpl implements InvestmentMapper {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    private static final String NAMESPACE = "com.hrp.efficiency.mapper.InvestmentMapper";

    @Override
    public List<?> selectAll() {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAll");
    }

    @Override
    public Object selectById(Long id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }

    @Override
    public int insert(Map<String, Object> data) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", data);
    }

    @Override
    public int updateById(Map<String, Object> data) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", data);
    }
}





