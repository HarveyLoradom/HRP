package com.hrp.reimb.mapper.impl;

import com.hrp.reimb.mapper.CtrlPayoutDetailMapper;
import com.hrp.common.entity.CtrlPayoutDetail;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CtrlPayoutDetailMapperImpl implements CtrlPayoutDetailMapper {
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    private static final String NAMESPACE = "com.hrp.reimb.mapper.CtrlPayoutDetailMapper";

    @Override
    public CtrlPayoutDetail selectById(Long id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }

    @Override
    public List<CtrlPayoutDetail> selectByPayoutId(Long payoutId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByPayoutId", payoutId);
    }

    @Override
    public int insert(CtrlPayoutDetail detail) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", detail);
    }

    @Override
    public int updateById(CtrlPayoutDetail detail) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", detail);
    }

    @Override
    public int deleteById(Long id) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", id);
    }

    @Override
    public int deleteByPayoutId(Long payoutId) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteByPayoutId", payoutId);
    }
}

