package com.hrp.budg.mapper.impl;

import com.hrp.budg.mapper.BudgetItemSubjectMapper;
import com.hrp.common.entity.BudgetItemSubject;
import com.hrp.common.entity.BudgetSubject;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 预算项目主体分配Mapper实现
 */
@Repository
public class BudgetItemSubjectMapperImpl implements BudgetItemSubjectMapper {

    @Autowired
    private SqlSession sqlSession;

    @Override
    public List<BudgetSubject> selectSubjectsByItemId(Long itemId) {
        return sqlSession.selectList("com.hrp.budg.mapper.BudgetItemSubjectMapper.selectSubjectsByItemId", itemId);
    }

    @Override
    public int deleteByItemId(Long itemId) {
        return sqlSession.delete("com.hrp.budg.mapper.BudgetItemSubjectMapper.deleteByItemId", itemId);
    }

    @Override
    public int insert(BudgetItemSubject itemSubject) {
        return sqlSession.insert("com.hrp.budg.mapper.BudgetItemSubjectMapper.insert", itemSubject);
    }

    @Override
    public int insertBatch(List<BudgetItemSubject> list) {
        return sqlSession.insert("com.hrp.budg.mapper.BudgetItemSubjectMapper.insertBatch", list);
    }
}

