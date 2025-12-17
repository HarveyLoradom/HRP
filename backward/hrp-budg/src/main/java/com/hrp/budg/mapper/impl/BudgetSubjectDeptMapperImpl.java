package com.hrp.budg.mapper.impl;

import com.hrp.budg.mapper.BudgetSubjectDeptMapper;
import com.hrp.common.entity.BudgetSubjectDept;
import com.hrp.common.entity.Dept;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BudgetSubjectDeptMapperImpl implements BudgetSubjectDeptMapper {
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    private static final String NAMESPACE = "com.hrp.budg.mapper.BudgetSubjectDeptMapper";

    @Override
    public List<BudgetSubjectDept> selectBySubjectId(Long subjectId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectBySubjectId", subjectId);
    }

    @Override
    public int insert(BudgetSubjectDept relation) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", relation);
    }

    @Override
    public int deleteBySubjectId(Long subjectId) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteBySubjectId", subjectId);
    }

    @Override
    public List<Dept> selectDeptsByIds(List<Long> deptIds) {
        if (deptIds == null || deptIds.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectDeptsByIds", deptIds);
    }
}

