package com.hrp.reimb.mapper.impl;

import com.hrp.reimb.mapper.CtrlPayoutPaymentMapper;
import com.hrp.common.entity.CtrlPayoutPayment;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CtrlPayoutPaymentMapperImpl implements CtrlPayoutPaymentMapper {
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    private static final String NAMESPACE = "com.hrp.reimb.mapper.CtrlPayoutPaymentMapper";

    @Override
    public CtrlPayoutPayment selectById(Long id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }

    @Override
    public List<CtrlPayoutPayment> selectByPayoutId(Long payoutId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByPayoutId", payoutId);
    }

    @Override
    public int insert(CtrlPayoutPayment payment) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", payment);
    }

    @Override
    public int updateById(CtrlPayoutPayment payment) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", payment);
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













