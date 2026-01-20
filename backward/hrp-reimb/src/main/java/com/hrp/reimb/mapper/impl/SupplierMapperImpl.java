package com.hrp.reimb.mapper.impl;

import com.hrp.reimb.mapper.SupplierMapper;
import com.hrp.common.entity.Supplier;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 供应商数据访问实现类
 */
@Repository
public class SupplierMapperImpl implements SupplierMapper {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    private static final String NAMESPACE = "com.hrp.reimb.mapper.SupplierMapper";

    @Override
    public Supplier selectById(Long id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }

    @Override
    public Supplier selectByCode(String code) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectByCode", code);
    }

    @Override
    public List<Supplier> selectAll() {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAll");
    }

    @Override
    public List<Supplier> selectByConditions(String supplierCode, String supplierName, Long isStop) {
        Map<String, Object> params = new HashMap<>();
        params.put("supplierCode", supplierCode);
        params.put("supplierName", supplierName);
        params.put("isStop", isStop);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByConditions", params);
    }

    @Override
    public int insert(Supplier supplier) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", supplier);
    }

    @Override
    public int updateById(Supplier supplier) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", supplier);
    }

    @Override
    public int deleteById(Long id) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", id);
    }
}

