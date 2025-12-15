package com.hrp.hr.mapper.impl;

import com.hrp.hr.mapper.AttendanceMapper;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AttendanceMapperImpl implements AttendanceMapper {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    private static final String NAMESPACE = "com.hrp.hr.mapper.AttendanceMapper";

    @Override
    public List<?> selectByEmpIdAndMonth(Long empId, String month) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByEmpIdAndMonth", 
            new java.util.HashMap<String, Object>() {{
                put("empId", empId);
                put("month", month);
            }});
    }
}





