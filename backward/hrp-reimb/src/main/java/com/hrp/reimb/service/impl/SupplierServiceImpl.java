package com.hrp.reimb.service.impl;

import com.hrp.reimb.mapper.SupplierMapper;
import com.hrp.reimb.service.SupplierService;
import com.hrp.common.entity.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 供应商服务实现类
 */
@Service
public class SupplierServiceImpl implements SupplierService {

    @Autowired
    private SupplierMapper supplierMapper;

    @Override
    public Supplier getById(Long id) {
        return supplierMapper.selectById(id);
    }

    @Override
    public Supplier getByCode(String code) {
        return supplierMapper.selectByCode(code);
    }

    @Override
    public List<Supplier> getAll() {
        return supplierMapper.selectAll();
    }

    @Override
    public List<Supplier> getByConditions(String supplierCode, String supplierName, Long isStop) {
        return supplierMapper.selectByConditions(supplierCode, supplierName, isStop);
    }

    @Override
    @Transactional
    public boolean save(Supplier supplier) {
        return supplierMapper.insert(supplier) > 0;
    }

    @Override
    @Transactional
    public boolean update(Supplier supplier) {
        return supplierMapper.updateById(supplier) > 0;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        return supplierMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional
    public boolean stop(Long id) {
        Supplier supplier = supplierMapper.selectById(id);
        if (supplier == null) {
            return false;
        }
        supplier.setIsStop(1L);
        return supplierMapper.updateById(supplier) > 0;
    }

    @Override
    @Transactional
    public boolean start(Long id) {
        Supplier supplier = supplierMapper.selectById(id);
        if (supplier == null) {
            return false;
        }
        supplier.setIsStop(0L);
        return supplierMapper.updateById(supplier) > 0;
    }
}

