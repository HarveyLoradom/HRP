package com.hrp.user.mapper.impl;

import com.hrp.user.mapper.DeptMapper;
import com.hrp.common.entity.Dept;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 部门数据访问实现类
 */
@Repository
public class DeptMapperImpl implements DeptMapper {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    private static final String NAMESPACE = "com.hrp.user.mapper.DeptMapper";

    @Override
    public Dept selectById(Long deptId) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", deptId);
    }

    @Override
    public Dept selectByCode(String deptCode) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectByCode", deptCode);
    }

    @Override
    public List<Dept> selectAll() {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAll");
    }

    @Override
    public List<Dept> selectBySuperDeptCode(String superDeptCode) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectBySuperDeptCode", superDeptCode);
    }

    @Override
    public int insert(Dept dept) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", dept);
    }

    @Override
    public int updateById(Dept dept) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", dept);
    }

    @Override
    public int deleteById(Long deptId) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", deptId);
    }
}
