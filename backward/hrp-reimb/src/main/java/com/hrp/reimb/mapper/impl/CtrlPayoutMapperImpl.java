package com.hrp.reimb.mapper.impl;

import com.hrp.reimb.mapper.CtrlPayoutMapper;
import com.hrp.common.entity.CtrlPayout;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CtrlPayoutMapperImpl implements CtrlPayoutMapper {
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    private static final String NAMESPACE = "com.hrp.reimb.mapper.CtrlPayoutMapper";

    @Override
    public CtrlPayout selectById(Long id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }

    @Override
    public CtrlPayout selectByBillcode(String billcode) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectByBillcode", billcode);
    }

    @Override
    public List<CtrlPayout> selectByEmpId(Long empId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByEmpId", empId);
    }

    @Override
    public List<CtrlPayout> selectByStatus(String status) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByStatus", status);
    }

    @Override
    public List<CtrlPayout> selectAll() {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAll");
    }

    @Override
    public List<CtrlPayout> selectByApprover(String userId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByApprover", userId);
    }

    @Override
    public int insert(CtrlPayout ctrlPayout) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", ctrlPayout);
    }

    @Override
    public int updateById(CtrlPayout ctrlPayout) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", ctrlPayout);
    }

    @Override
    public int deleteById(Long id) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", id);
    }
}

