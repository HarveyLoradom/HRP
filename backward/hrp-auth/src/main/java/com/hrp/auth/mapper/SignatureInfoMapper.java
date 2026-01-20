package com.hrp.auth.mapper;

import com.hrp.common.entity.SignatureInfo;
import org.apache.ibatis.annotations.Param;

/**
 * 手写签名信息数据访问接口
 */
public interface SignatureInfoMapper {
    SignatureInfo selectById(@Param("id") Long id);
    SignatureInfo selectByEmpId(@Param("empId") Long empId);
    int insert(SignatureInfo signatureInfo);
    int updateById(SignatureInfo signatureInfo);
    int deleteById(@Param("id") Long id);
    int deleteByEmpId(@Param("empId") Long empId);
}

