package com.hrp.budg.mapper.impl;

import com.hrp.budg.mapper.BudgetSubjectRelationMapper;
import com.hrp.common.entity.BudgetSubjectRelation;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BudgetSubjectRelationMapperImpl implements BudgetSubjectRelationMapper {
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    private static final String NAMESPACE = "com.hrp.budg.mapper.BudgetSubjectRelationMapper";

    @Override
    public List<BudgetSubjectRelation> selectBySubjectId(Long subjectId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectBySubjectId", subjectId);
    }

    @Override
    public int insert(BudgetSubjectRelation relation) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", relation);
    }

    @Override
    public int deleteBySubjectId(Long subjectId) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteBySubjectId", subjectId);
    }
}

