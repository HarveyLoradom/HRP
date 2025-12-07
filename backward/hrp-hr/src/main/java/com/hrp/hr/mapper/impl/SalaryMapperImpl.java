package com.hrp.hr.mapper.impl;

import com.hrp.hr.mapper.SalaryMapper;
import com.hrp.common.entity.Salary;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class SalaryMapperImpl implements SalaryMapper {
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    private static final String NAMESPACE = "com.hrp.hr.mapper.SalaryMapper";

    @Override
    public Salary selectById(Long id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }

    @Override
    public Salary selectByEmpIdAndMonth(Long empId, String salaryMonth) {
        Map<String, Object> params = new HashMap<>();
        params.put("empId", empId);
        params.put("salaryMonth", salaryMonth);
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectByEmpIdAndMonth", params);
    }

    @Override
    public List<Salary> selectByEmpId(Long empId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByEmpId", empId);
    }

    @Override
    public List<Salary> selectByMonth(String salaryMonth) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByMonth", salaryMonth);
    }

    @Override
    public List<Salary> selectAll() {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAll");
    }

    @Override
    public int insert(Salary salary) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", salary);
    }

    @Override
    public int updateById(Salary salary) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", salary);
    }

    @Override
    public int deleteById(Long id) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", id);
    }
}

