package com.hrp.auth.mapper.impl;

import com.hrp.auth.mapper.TemplateConfigMapper;
import com.hrp.common.entity.TemplateConfig;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 模板设置数据访问实现类
 */
@Repository
public class TemplateConfigMapperImpl implements TemplateConfigMapper {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    private static final String NAMESPACE = "com.hrp.auth.mapper.TemplateConfigMapper";

    @Override
    public TemplateConfig selectById(Long configId) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", configId);
    }

    @Override
    public TemplateConfig selectByBusinessType(String businessType, String businessTypeValue) {
        Map<String, Object> params = new HashMap<>();
        params.put("businessType", businessType);
        params.put("businessTypeValue", businessTypeValue);
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectByBusinessType", params);
    }

    @Override
    public List<TemplateConfig> selectByBusinessTypeOnly(String businessType) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByBusinessTypeOnly", businessType);
    }

    @Override
    public List<TemplateConfig> selectAll() {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAll");
    }

    @Override
    public List<TemplateConfig> selectAllPage(Long offset, Long size) {
        Map<String, Object> params = new HashMap<>();
        params.put("offset", offset);
        params.put("size", size);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAllPage", params);
    }

    @Override
    public Long countAll() {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".countAll");
    }

    @Override
    public int insert(TemplateConfig config) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", config);
    }

    @Override
    public int updateById(TemplateConfig config) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", config);
    }

    @Override
    public int deleteById(Long configId) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", configId);
    }
}

