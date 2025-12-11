package com.hrp.user.mapper.impl;

import com.hrp.common.entity.UserWithEmployee;
import com.hrp.user.mapper.UserEmployeeMapper;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户和员工关联查询Mapper实现
 */
@Repository
public class UserEmployeeMapperImpl implements UserEmployeeMapper {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    private static final String NAMESPACE = "com.hrp.user.mapper.UserEmployeeMapper";

    @Override
    public List<UserWithEmployee> selectAllEmployeesWithUser() {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAllEmployeesWithUser");
    }

    @Override
    public List<UserWithEmployee> selectEmployeesByKeyword(String keyword) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectEmployeesByKeyword", keyword);
    }
}

