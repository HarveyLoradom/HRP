package com.hrp.user.mapper.impl;

import com.hrp.user.mapper.ClinicMapper;
import com.hrp.common.entity.Clinic;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 科室数据访问实现类
 */
@Repository
public class ClinicMapperImpl implements ClinicMapper {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    private static final String NAMESPACE = "com.hrp.user.mapper.ClinicMapper";

    @Override
    public Clinic selectById(Long clinicId) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", clinicId);
    }

    @Override
    public Clinic selectByCode(String clinicCode) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectByCode", clinicCode);
    }

    @Override
    public List<Clinic> selectAll() {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAll");
    }

    @Override
    public List<Clinic> selectByDeptId(Long deptId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByDeptId", deptId);
    }

    @Override
    public int insert(Clinic clinic) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", clinic);
    }

    @Override
    public int updateById(Clinic clinic) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", clinic);
    }

    @Override
    public int deleteById(Long clinicId) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", clinicId);
    }
}
