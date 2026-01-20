package com.hrp.reimb.mapper.impl;

import com.hrp.reimb.mapper.CtrlPayoutInvoiceMapper;
import com.hrp.common.entity.CtrlPayoutInvoice;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CtrlPayoutInvoiceMapperImpl implements CtrlPayoutInvoiceMapper {
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    private static final String NAMESPACE = "com.hrp.reimb.mapper.CtrlPayoutInvoiceMapper";

    @Override
    public CtrlPayoutInvoice selectById(Long id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }

    @Override
    public List<CtrlPayoutInvoice> selectByPayoutId(Long payoutId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByPayoutId", payoutId);
    }

    @Override
    public int insert(CtrlPayoutInvoice invoice) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", invoice);
    }

    @Override
    public int updateById(CtrlPayoutInvoice invoice) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", invoice);
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













