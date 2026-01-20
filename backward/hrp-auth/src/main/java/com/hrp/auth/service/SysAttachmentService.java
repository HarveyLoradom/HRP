package com.hrp.auth.service;

import com.hrp.common.entity.SysAttachment;

import java.util.List;

/**
 * 附件服务接口
 */
public interface SysAttachmentService {
    SysAttachment getById(Long id);
    List<SysAttachment> getByBusiness(String businessType, String businessId);
    List<SysAttachment> getByBusinessId(String businessId); // 只根据businessId查询
    boolean save(SysAttachment attachment);
    boolean saveBatch(List<SysAttachment> attachments);
    boolean update(SysAttachment attachment);
    boolean updateBusinessId(Long attachmentId, String businessId);
    boolean updateBusinessIdBatch(String businessType, String businessId, List<Long> attachmentIds);
    boolean delete(Long id);
    boolean deleteByBusiness(String businessType, String businessId);
    boolean deleteByBusinessId(String businessId); // 只根据businessId删除
}

