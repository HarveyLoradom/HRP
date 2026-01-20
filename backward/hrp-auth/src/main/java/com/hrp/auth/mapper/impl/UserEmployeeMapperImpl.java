package com.hrp.auth.mapper.impl;

import com.hrp.auth.mapper.UserEmployeeMapper;
import com.hrp.common.entity.Employee;
import com.hrp.common.entity.UserWithEmployee;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户和员工关联查询Mapper实现类
 */
@Repository
public class UserEmployeeMapperImpl implements UserEmployeeMapper {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    private static final String NAMESPACE = "com.hrp.auth.mapper.UserEmployeeMapper";

    @Override
    public List<UserWithEmployee> selectAllEmployeesWithUser(Long isStop) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAllEmployeesWithUser", isStop);
    }

    @Override
    public List<UserWithEmployee> selectEmployeesByKeyword(String keyword, Long isStop) {
        Map<String, Object> params = new HashMap<>();
        params.put("keyword", keyword);
        params.put("isStop", isStop);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectEmployeesByKeyword", params);
    }

    @Override
    public List<UserWithEmployee> selectAllEmployeesWithUserPage(Long isStop, Long offset, Long size) {
        Map<String, Object> params = new HashMap<>();
        params.put("isStop", isStop);
        params.put("offset", offset);
        params.put("size", size);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAllEmployeesWithUserPage", params);
    }

    @Override
    public List<UserWithEmployee> selectEmployeesByKeywordPage(String keyword, Long isStop, Long offset, Long size) {
        Map<String, Object> params = new HashMap<>();
        params.put("keyword", keyword);
        params.put("isStop", isStop);
        params.put("offset", offset);
        params.put("size", size);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectEmployeesByKeywordPage", params);
    }

    @Override
    public Long countAllEmployeesWithUser(Long isStop) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".countAllEmployeesWithUser", isStop);
    }

    @Override
    public Long countEmployeesByKeyword(String keyword, Long isStop) {
        Map<String, Object> params = new HashMap<>();
        params.put("keyword", keyword);
        params.put("isStop", isStop);
        return sqlSessionTemplate.selectOne(NAMESPACE + ".countEmployeesByKeyword", params);
    }

    @Override
    public Employee selectEmployeeByCode(String empCode) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectEmployeeByCode", empCode);
    }

    @Override
    public List<Employee> selectAllEmployees() {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAllEmployees");
    }

    @Override
    public int insertEmployee(Employee employee) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insertEmployee", employee);
    }

    @Override
    public int updateEmployee(Employee employee) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateEmployee", employee);
    }

    @Override
    public int deleteEmployeeByCode(String empCode) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteEmployeeByCode", empCode);
    }
}

