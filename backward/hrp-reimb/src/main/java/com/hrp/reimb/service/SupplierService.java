package com.hrp.reimb.service;

import com.hrp.common.entity.Supplier;

import java.util.List;

/**
 * 供应商服务接口
 */
public interface SupplierService {
    Supplier getById(Long id);
    Supplier getByCode(String code);
    List<Supplier> getAll();
    List<Supplier> getByConditions(String supplierCode, String supplierName, Long isStop);
    boolean save(Supplier supplier);
    boolean update(Supplier supplier);
    boolean delete(Long id);
    boolean stop(Long id);
    boolean start(Long id);
}

