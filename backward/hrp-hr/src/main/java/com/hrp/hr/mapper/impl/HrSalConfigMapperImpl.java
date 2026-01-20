package com.hrp.hr.mapper.impl;

import com.hrp.hr.mapper.HrSalConfigMapper;
import com.hrp.common.entity.HrSalConfig;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 薪酬配置表数据访问实现类
 */
@Repository
public class HrSalConfigMapperImpl implements HrSalConfigMapper {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    private static final String NAMESPACE = "com.hrp.hr.mapper.HrSalConfigMapper";

    @Override
    public HrSalConfig selectById(Integer configId) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", configId);
    }

    @Override
    public HrSalConfig selectByEmpId(Long empId) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectByEmpId", empId);
    }

    @Override
    public List<HrSalConfig> selectByConditions(Long empId, String empCode, String empName) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("empId", empId);
        params.put("empCode", empCode);
        params.put("empName", empName);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByConditions", params);
    }

    @Override
    public int insert(HrSalConfig config) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", config);
    }

    @Override
    public int updateById(HrSalConfig config) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", config);
    }

    @Override
    public int deleteById(Integer configId) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", configId);
    }

    @Override
    public int deleteByEmpId(Long empId) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteByEmpId", empId);
    }

    @Override
    public List<Long> selectAllEmpIds() {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAllEmpIds");
    }
}

