package com.hrp.auth.mapper.impl;

import com.hrp.auth.mapper.PrintTemplateMapper;
import com.hrp.common.entity.PrintTemplate;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 打印模板数据访问实现类
 */
@Repository
public class PrintTemplateMapperImpl implements PrintTemplateMapper {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    private static final String NAMESPACE = "com.hrp.auth.mapper.PrintTemplateMapper";

    @Override
    public PrintTemplate selectById(Long templateId) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", templateId);
    }

    @Override
    public PrintTemplate selectByCode(String templateCode) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectByCode", templateCode);
    }

    @Override
    public List<PrintTemplate> selectByType(String templateType) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByType", templateType);
    }

    @Override
    public List<PrintTemplate> selectAll() {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAll");
    }

    @Override
    public PrintTemplate selectDefaultByType(String templateType) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectDefaultByType", templateType);
    }

    @Override
    public int insert(PrintTemplate template) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", template);
    }

    @Override
    public int updateById(PrintTemplate template) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", template);
    }

    @Override
    public int deleteById(Long templateId) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", templateId);
    }

    @Override
    public java.util.Map<String, Object> selectDataByBusinessKey(String templateType, String businessKey) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectDataByBusinessKey", 
            new java.util.HashMap<String, Object>() {{
                put("templateType", templateType);
                put("businessKey", businessKey);
            }});
    }
}

