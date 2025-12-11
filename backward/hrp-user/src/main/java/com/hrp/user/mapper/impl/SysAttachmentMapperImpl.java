package com.hrp.user.mapper.impl;

import com.hrp.user.mapper.SysAttachmentMapper;
import com.hrp.common.entity.SysAttachment;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class SysAttachmentMapperImpl implements SysAttachmentMapper {
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    private static final String NAMESPACE = "com.hrp.user.mapper.SysAttachmentMapper";

    @Override
    public SysAttachment selectById(Long id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }

    @Override
    public List<SysAttachment> selectByBusiness(String businessType, Long businessId) {
        Map<String, Object> params = new HashMap<>();
        params.put("businessType", businessType);
        params.put("businessId", businessId);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByBusiness", params);
    }

    @Override
    public int insert(SysAttachment attachment) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", attachment);
    }

    @Override
    public int updateById(SysAttachment attachment) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", attachment);
    }

    @Override
    public int deleteById(Long id) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", id);
    }

    @Override
    public int deleteByBusiness(String businessType, Long businessId) {
        Map<String, Object> params = new HashMap<>();
        params.put("businessType", businessType);
        params.put("businessId", businessId);
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteByBusiness", params);
    }
}




