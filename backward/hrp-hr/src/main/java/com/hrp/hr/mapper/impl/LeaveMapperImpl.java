package com.hrp.hr.mapper.impl;

import com.hrp.hr.mapper.LeaveMapper;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class LeaveMapperImpl implements LeaveMapper {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    private static final String NAMESPACE = "com.hrp.hr.mapper.LeaveMapper";

    @Override
    public List<?> selectByEmpId(Long empId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByEmpId", empId);
    }

    @Override
    public Object selectById(Long id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }

    @Override
    public List<?> selectRecordsByLeaveId(Long leaveId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectRecordsByLeaveId", leaveId);
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





