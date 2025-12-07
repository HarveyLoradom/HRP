package com.hrp.user.mapper.impl;

import com.hrp.user.mapper.EmployeeMapper;
import com.hrp.common.entity.Employee;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 职工数据访问实现类
 */
@Repository
public class EmployeeMapperImpl implements EmployeeMapper {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    private static final String NAMESPACE = "com.hrp.user.mapper.EmployeeMapper";

    @Override
    public Employee selectById(Long empId) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", empId);
    }

    @Override
    public Employee selectByCode(String empCode) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectByCode", empCode);
    }

    @Override
    public List<Employee> selectAll() {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAll");
    }

    @Override
    public List<Employee> selectByDeptId(Long deptId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByDeptId", deptId);
    }

    @Override
    public int insert(Employee employee) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", employee);
    }

    @Override
    public int updateById(Employee employee) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", employee);
    }

    @Override
    public int deleteById(Long empId) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", empId);
    }
}
