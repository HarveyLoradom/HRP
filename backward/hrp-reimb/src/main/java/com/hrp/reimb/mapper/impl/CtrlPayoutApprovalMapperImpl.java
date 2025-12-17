package com.hrp.reimb.mapper.impl;

import com.hrp.reimb.mapper.CtrlPayoutApprovalMapper;
import com.hrp.common.entity.CtrlPayoutApproval;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CtrlPayoutApprovalMapperImpl implements CtrlPayoutApprovalMapper {
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    private static final String NAMESPACE = "com.hrp.reimb.mapper.CtrlPayoutApprovalMapper";

    @Override
    public CtrlPayoutApproval selectById(Long id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }

    @Override
    public List<CtrlPayoutApproval> selectByPayoutId(Long payoutId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByPayoutId", payoutId);
    }

    @Override
    public int insert(CtrlPayoutApproval approval) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", approval);
    }

    @Override
    public int updateById(CtrlPayoutApproval approval) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", approval);
    }

    @Override
    public int deleteById(Long id) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", id);
    }
}













