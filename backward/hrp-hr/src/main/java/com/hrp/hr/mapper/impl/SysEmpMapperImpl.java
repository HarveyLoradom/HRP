package com.hrp.hr.mapper.impl;

import com.hrp.hr.mapper.SysEmpMapper;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 员工相关查询实现（用于考勤等模块）
 */
@Repository
public class SysEmpMapperImpl implements SysEmpMapper {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    private static final String NAMESPACE = "com.hrp.hr.mapper.SysEmpMapper";

    @Override
    public List<Long> selectActiveEmpIds() {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectActiveEmpIds");
    }

    @Override
    public Long selectEmpIdByCode(String empCode) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectEmpIdByCode", empCode);
    }
}


