package com.hrp.budg.mapper.impl;

import com.hrp.budg.mapper.BudgetSubjectMapper;
import com.hrp.common.entity.BudgetSubject;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BudgetSubjectMapperImpl implements BudgetSubjectMapper {
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    private static final String NAMESPACE = "com.hrp.budg.mapper.BudgetSubjectMapper";

    @Override
    public BudgetSubject selectById(Long id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }

    @Override
    public BudgetSubject selectByCode(String code) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectByCode", code);
    }

    @Override
    public List<BudgetSubject> selectAll() {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAll");
    }

    @Override
    public List<BudgetSubject> selectByParentId(Long parentId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByParentId", parentId);
    }

    @Override
    public List<BudgetSubject> selectByDeptId(Long deptId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByDeptId", deptId);
    }

    @Override
    public List<BudgetSubject> selectTree() {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectTree");
    }

    @Override
    public int insert(BudgetSubject budgetSubject) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", budgetSubject);
    }

    @Override
    public int updateById(BudgetSubject budgetSubject) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", budgetSubject);
    }

    @Override
    public int deleteById(Long id) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", id);
    }
}








