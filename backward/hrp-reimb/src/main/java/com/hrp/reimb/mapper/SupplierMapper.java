package com.hrp.reimb.mapper;

import com.hrp.common.entity.Supplier;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 供应商数据访问接口
 */
public interface SupplierMapper {
    Supplier selectById(@Param("id") Long id);
    Supplier selectByCode(@Param("code") String code);
    List<Supplier> selectAll();
    List<Supplier> selectByConditions(@Param("supplierCode") String supplierCode,
                                      @Param("supplierName") String supplierName, 
                                      @Param("isStop") Long isStop);
    int insert(Supplier supplier);
    int updateById(Supplier supplier);
    int deleteById(@Param("id") Long id);
}

