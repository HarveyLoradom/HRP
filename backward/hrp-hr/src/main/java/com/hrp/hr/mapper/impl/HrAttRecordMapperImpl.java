package com.hrp.hr.mapper.impl;

import com.hrp.hr.mapper.HrAttRecordMapper;
import com.hrp.common.entity.HrAttRecord;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 员工考勤记录表数据访问实现类
 */
@Repository
public class HrAttRecordMapperImpl implements HrAttRecordMapper {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    private static final String NAMESPACE = "com.hrp.hr.mapper.HrAttRecordMapper";

    @Override
    public HrAttRecord selectById(Long recordId) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", recordId);
    }

    @Override
    public List<HrAttRecord> selectByConditions(Long empId, String attDate, String startDate, 
                                                 String endDate, String attStatus, String attType, String attSubType) {
        Map<String, Object> params = new HashMap<>();
        params.put("empId", empId);
        params.put("attDate", attDate);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("attStatus", attStatus);
        params.put("attType", attType);
        params.put("attSubType", attSubType);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByConditions", params);
    }

    @Override
    public HrAttRecord selectTodayByEmpId(Long empId, String attDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("empId", empId);
        params.put("attDate", attDate);
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectTodayByEmpId", params);
    }

    @Override
    public List<Map<String, Object>> selectSummaryByRange(String startDate, String endDate) {
        Map<String, Object> params = new HashMap<>();
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectSummaryByRange", params);
    }

    @Override
    public int insert(HrAttRecord record) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", record);
    }

    @Override
    public int updateById(HrAttRecord record) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", record);
    }

    @Override
    public int deleteById(Long recordId) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", recordId);
    }
}

