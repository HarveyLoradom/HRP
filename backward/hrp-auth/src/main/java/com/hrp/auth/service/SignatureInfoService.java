package com.hrp.auth.service;

import com.hrp.common.entity.SignatureInfo;

/**
 * 手写签名信息服务接口
 */
public interface SignatureInfoService {
    SignatureInfo getById(Long id);
    SignatureInfo getByEmpId(Long empId);
    boolean save(SignatureInfo signatureInfo);
    boolean update(SignatureInfo signatureInfo);
    boolean delete(Long id);
    boolean deleteByEmpId(Long empId);
}

