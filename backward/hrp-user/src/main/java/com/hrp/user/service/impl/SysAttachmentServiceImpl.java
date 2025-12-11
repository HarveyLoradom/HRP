package com.hrp.user.service.impl;

import com.hrp.user.mapper.SysAttachmentMapper;
import com.hrp.user.service.SysAttachmentService;
import com.hrp.common.entity.SysAttachment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SysAttachmentServiceImpl implements SysAttachmentService {

    @Autowired
    private SysAttachmentMapper sysAttachmentMapper;

    @Override
    public SysAttachment getById(Long id) {
        return sysAttachmentMapper.selectById(id);
    }

    @Override
    public List<SysAttachment> getByBusiness(String businessType, Long businessId) {
        return sysAttachmentMapper.selectByBusiness(businessType, businessId);
    }

    @Override
    @Transactional
    public boolean save(SysAttachment attachment) {
        return sysAttachmentMapper.insert(attachment) > 0;
    }

    @Override
    @Transactional
    public boolean saveBatch(List<SysAttachment> attachments) {
        for (SysAttachment attachment : attachments) {
            if (sysAttachmentMapper.insert(attachment) <= 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    @Transactional
    public boolean update(SysAttachment attachment) {
        return sysAttachmentMapper.updateById(attachment) > 0;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        return sysAttachmentMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional
    public boolean deleteByBusiness(String businessType, Long businessId) {
        return sysAttachmentMapper.deleteByBusiness(businessType, businessId) > 0;
    }
}




