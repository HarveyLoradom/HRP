package com.hrp.hr.mapper.impl;

import com.hrp.hr.mapper.HrSalCalculateMapper;
import com.hrp.common.entity.HrSalCalculate;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 薪酬核算表数据访问实现类
 */
@Repository
public class HrSalCalculateMapperImpl implements HrSalCalculateMapper {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    private static final String NAMESPACE = "com.hrp.hr.mapper.HrSalCalculateMapper";

    @Override
    public HrSalCalculate selectById(Long calcId) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", calcId);
    }

    @Override
    public HrSalCalculate selectByEmpIdAndMonth(Long empId, String calcMonth) {
        Map<String, Object> params = new HashMap<>();
        params.put("empId", empId);
        params.put("calcMonth", calcMonth);
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectByEmpIdAndMonth", params);
    }

    @Override
    public List<HrSalCalculate> selectByConditions(Long empId, String empCode, String empName, String calcMonth, String calcStatus) {
        Map<String, Object> params = new HashMap<>();
        params.put("empId", empId);
        params.put("empCode", empCode);
        params.put("empName", empName);
        params.put("calcMonth", calcMonth);
        params.put("calcStatus", calcStatus);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByConditions", params);
    }

    @Override
    public int insert(HrSalCalculate calculate) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", calculate);
    }

    @Override
    public int updateById(HrSalCalculate calculate) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", calculate);
    }

    @Override
    public int deleteById(Long calcId) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", calcId);
    }
}

