package com.hrp.budg.mapper.impl;

import com.hrp.budg.mapper.BudgetDetailMapper;
import com.hrp.common.entity.BudgetDetail;
import com.hrp.common.entity.BudgetExecutionDetail;
import com.hrp.common.entity.BudgetApplyDetail;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class BudgetDetailMapperImpl implements BudgetDetailMapper {
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    private static final String NAMESPACE = "com.hrp.budg.mapper.BudgetDetailMapper";

    @Override
    public List<BudgetDetail> selectPage(long offset, long size, Map<String, Object> params) {
        if (params == null) {
            params = new HashMap<>();
        }
        // 将 params 中的参数直接展开到 queryParams 中
        Map<String, Object> queryParams = new HashMap<>(params);
        queryParams.put("offset", offset);
        queryParams.put("size", size);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectPage", queryParams);
    }

    @Override
    public long countByParams(Map<String, Object> params) {
        if (params == null) {
            params = new HashMap<>();
        }
        // 直接传递 params，在 XML 中使用 key 名称访问
        return sqlSessionTemplate.selectOne(NAMESPACE + ".countByParams", params);
    }

    @Override
    public List<BudgetExecutionDetail> selectExecutionDetails(Long itemId, Long subjectId) {
        Map<String, Object> params = new HashMap<>();
        params.put("itemId", itemId);
        params.put("subjectId", subjectId);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectExecutionDetails", params);
    }

    @Override
    public List<BudgetApplyDetail> selectApplyDetails(Long itemId, Long subjectId) {
        Map<String, Object> params = new HashMap<>();
        params.put("itemId", itemId);
        params.put("subjectId", subjectId);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectApplyDetails", params);
    }
}

