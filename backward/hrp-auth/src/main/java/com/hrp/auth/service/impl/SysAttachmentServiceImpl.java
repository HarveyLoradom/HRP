package com.hrp.auth.service.impl;

import com.hrp.auth.mapper.SysAttachmentMapper;
import com.hrp.auth.service.SysAttachmentService;
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
    public List<SysAttachment> getByBusiness(String businessType, String businessId) {
        System.out.println("=== 查询附件 ===");
        System.out.println("businessType: " + businessType);
        System.out.println("businessId: " + businessId);
        List<SysAttachment> attachments = sysAttachmentMapper.selectByBusiness(businessId);
        System.out.println("查询结果数量: " + (attachments != null ? attachments.size() : 0));
        System.out.println("=== 查询附件完成 ===");
        return attachments;
    }

    @Override
    public List<SysAttachment> getByBusinessId(String businessId) {
        System.out.println("=== 根据businessId查询附件 ===");
        System.out.println("businessId: " + businessId);
        List<SysAttachment> attachments = sysAttachmentMapper.selectByBusiness(businessId);
        System.out.println("查询结果数量: " + (attachments != null ? attachments.size() : 0));
        System.out.println("=== 查询附件完成 ===");
        return attachments;
    }

    @Override
    @Transactional
    public boolean save(SysAttachment attachment) {
        System.out.println("=== SysAttachmentServiceImpl.save ===");
        System.out.println("attachment: " + attachment);
        System.out.println("businessType: " + (attachment != null ? attachment.getBusinessType() : "null"));
        System.out.println("businessId: " + (attachment != null ? attachment.getBusinessId() : "null"));
        System.out.println("fileName: " + (attachment != null ? attachment.getFileName() : "null"));
        System.out.println("filePath: " + (attachment != null ? attachment.getFilePath() : "null"));
        
        try {
            int result = sysAttachmentMapper.insert(attachment);
            System.out.println("insert result: " + result);
            System.out.println("attachmentId after insert: " + (attachment != null ? attachment.getAttachmentId() : "null"));
            
            if (result > 0 && attachment != null && attachment.getAttachmentId() != null) {
                // 验证插入是否成功，通过查询验证
                SysAttachment saved = sysAttachmentMapper.selectById(attachment.getAttachmentId());
                if (saved != null) {
                    System.out.println("验证成功：附件记录已保存到数据库，attachmentId: " + saved.getAttachmentId());
                } else {
                    System.err.println("警告：附件记录插入后查询不到，可能插入失败");
                }
            }
            
            System.out.println("=== SysAttachmentServiceImpl.save 完成 ===");
            return result > 0;
        } catch (Exception e) {
            System.err.println("保存附件记录时发生异常：" + e.getMessage());
            e.printStackTrace();
            throw e;
        }
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
    public boolean updateBusinessId(Long attachmentId, String businessId) {
        return sysAttachmentMapper.updateBusinessIdById(attachmentId, businessId) > 0;
    }

    @Override
    @Transactional
    public boolean updateBusinessIdBatch(String businessType, String businessId, List<Long> attachmentIds) {
        System.out.println("=== SysAttachmentServiceImpl.updateBusinessIdBatch ===");
        System.out.println("businessType: " + businessType);
        System.out.println("businessId: " + businessId);
        System.out.println("attachmentIds: " + attachmentIds);
        
        if (attachmentIds == null || attachmentIds.isEmpty()) {
            System.out.println("attachmentIds为空，跳过更新");
            return true;
        }
        
        int updatedCount = sysAttachmentMapper.updateBusinessIdByTypeAndNull(businessType, businessId, attachmentIds);
        System.out.println("实际更新的记录数: " + updatedCount);
        
        boolean success = updatedCount > 0;
        System.out.println("更新结果: " + success);
        System.out.println("=== SysAttachmentServiceImpl.updateBusinessIdBatch完成 ===");
        return success;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        return sysAttachmentMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional
    public boolean deleteByBusiness(String businessType, String businessId) {
        return deleteByBusinessId(businessId);
    }

    @Override
    @Transactional
    public boolean deleteByBusinessId(String businessId) {
        // 先查询所有附件，以便删除文件
        List<SysAttachment> attachments = sysAttachmentMapper.selectByBusiness(businessId);
        
        // 用于记录文件所在的文件夹路径（如果businessId是时间戳，会有专门的文件夹）
        // 使用 Set 来避免重复记录同一个文件夹
        java.util.Set<java.io.File> attachmentFolders = new java.util.HashSet<>();
        
        // 删除文件系统中的文件
        if (attachments != null && !attachments.isEmpty()) {
            for (SysAttachment attachment : attachments) {
                String filePath = attachment.getFilePath();
                if (filePath != null && !filePath.trim().isEmpty()) {
                    try {
                        java.io.File file = new java.io.File(filePath);
                        if (file.exists() && file.isFile()) {
                            // 记录文件所在的文件夹（用于后续删除空文件夹）
                            java.io.File parentDir = file.getParentFile();
                            if (parentDir != null && parentDir.exists() && parentDir.isDirectory()) {
                                // 如果businessId是时间戳（纯数字），且文件夹名与businessId相同，记录这个文件夹
                                if (businessId != null && businessId.matches("^\\d+$") && 
                                    parentDir.getName().equals(businessId)) {
                                    attachmentFolders.add(parentDir);
                                }
                            }
                            
                            boolean deleted = file.delete();
                            if (!deleted) {
                                System.err.println("警告：删除附件文件失败：" + filePath);
                            } else {
                                System.out.println("成功删除附件文件：" + filePath);
                            }
                        }
                    } catch (Exception e) {
                        // 文件删除失败，但不影响数据库删除，只记录日志
                        System.err.println("警告：删除附件文件时出错：" + filePath + ", 错误：" + e.getMessage());
                    }
                }
            }
            
            // 删除文件后，检查并删除所有相关的空文件夹
            for (java.io.File attachmentFolder : attachmentFolders) {
                if (attachmentFolder != null && attachmentFolder.exists() && attachmentFolder.isDirectory()) {
                    try {
                        // 检查文件夹是否为空
                        String[] files = attachmentFolder.list();
                        if (files == null || files.length == 0) {
                            boolean folderDeleted = attachmentFolder.delete();
                            if (folderDeleted) {
                                System.out.println("成功删除附件文件夹：" + attachmentFolder.getAbsolutePath());
                            } else {
                                System.err.println("警告：删除附件文件夹失败：" + attachmentFolder.getAbsolutePath());
                            }
                        } else {
                            System.out.println("附件文件夹不为空，保留文件夹：" + attachmentFolder.getAbsolutePath() + "，剩余文件数：" + files.length);
                        }
                    } catch (Exception e) {
                        System.err.println("警告：删除附件文件夹时出错：" + attachmentFolder.getAbsolutePath() + ", 错误：" + e.getMessage());
                    }
                }
            }
        }
        
        // 删除数据库记录
        return sysAttachmentMapper.deleteByBusiness(businessId) > 0;
    }
}

