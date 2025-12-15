package com.hrp.hr.mapper.impl;

import com.hrp.hr.mapper.TransferMapper;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class TransferMapperImpl implements TransferMapper {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    private static final String NAMESPACE = "com.hrp.hr.mapper.TransferMapper";

    @Override
    public List<?> selectByEmpId(Long empId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByEmpId", empId);
    }

    @Override
    public Object selectById(Long id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }

    @Override
    public List<?> selectRecordsByTransferId(Long transferId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectRecordsByTransferId", transferId);
    }

    @Override
    public int insertRecord(Map<String, Object> record) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insertRecord", record);
    }

    @Override
    public int updateById(Map<String, Object> data) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", data);
    }
}





