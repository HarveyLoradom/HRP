package com.hrp.efficiency.mapper.impl;

import com.hrp.efficiency.mapper.DataCollectionMapper;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class DataCollectionMapperImpl implements DataCollectionMapper {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    private static final String NAMESPACE = "com.hrp.efficiency.mapper.DataCollectionMapper";

    @Override
    public int insert(Map<String, Object> data) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", data);
    }

    @Override
    public List<?> selectByType(String collectionType) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByType", collectionType);
    }
}





