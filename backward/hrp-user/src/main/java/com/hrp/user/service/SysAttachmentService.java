package com.hrp.user.service;

import com.hrp.common.entity.SysAttachment;

import java.util.List;

/**
 * 附件服务接口
 */
public interface SysAttachmentService {
    SysAttachment getById(Long id);
    List<SysAttachment> getByBusiness(String businessType, Long businessId);
    boolean save(SysAttachment attachment);
    boolean saveBatch(List<SysAttachment> attachments);
    boolean update(SysAttachment attachment);
    boolean delete(Long id);
    boolean deleteByBusiness(String businessType, Long businessId);
}




