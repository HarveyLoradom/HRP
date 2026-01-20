package com.hrp.auth.mapper.impl;

import com.hrp.auth.mapper.SignatureInfoMapper;
import com.hrp.common.entity.SignatureInfo;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class SignatureInfoMapperImpl implements SignatureInfoMapper {
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    private static final String NAMESPACE = "com.hrp.auth.mapper.SignatureInfoMapper";

    @Override
    public SignatureInfo selectById(Long id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }

    @Override
    public SignatureInfo selectByEmpId(Long empId) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectByEmpId", empId);
    }

    @Override
    public int insert(SignatureInfo signatureInfo) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", signatureInfo);
    }

    @Override
    public int updateById(SignatureInfo signatureInfo) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", signatureInfo);
    }

    @Override
    public int deleteById(Long id) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", id);
    }

    @Override
    public int deleteByEmpId(Long empId) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteByEmpId", empId);
    }
}

