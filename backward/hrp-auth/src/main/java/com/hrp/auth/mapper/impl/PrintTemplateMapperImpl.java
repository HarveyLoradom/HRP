package com.hrp.auth.mapper.impl;

import com.hrp.auth.mapper.PrintTemplateMapper;
import com.hrp.common.entity.PrintTemplate;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

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
    public List<PrintTemplate> selectByType(String templateType, Integer isActive) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("templateType", templateType);
        params.put("isActive", isActive);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByType", params);
    }

    @Override
    public List<PrintTemplate> selectAll(Integer isActive) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAll", isActive);
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

    @Override
    public List<Map<String, Object>> selectPaymentsByPayoutId(Long payoutId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectPaymentsByPayoutId", payoutId);
    }

    @Override
    public List<Map<String, Object>> selectInvoicesByPayoutId(Long payoutId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectInvoicesByPayoutId", payoutId);
    }

    @Override
    public List<Map<String, Object>> selectBudgetDetailsByBusinessNo(String businessNo) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectBudgetDetailsByBusinessNo", businessNo);
    }

    @Override
    public List<Map<String, Object>> selectAssetPurchaseApplyDetailsByApplyNo(String applyNo) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAssetPurchaseApplyDetailsByApplyNo", applyNo);
    }
}

