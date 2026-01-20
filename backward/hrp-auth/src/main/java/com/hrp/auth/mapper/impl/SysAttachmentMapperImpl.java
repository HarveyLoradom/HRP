package com.hrp.auth.mapper.impl;

import com.hrp.auth.mapper.SysAttachmentMapper;
import com.hrp.common.entity.SysAttachment;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class SysAttachmentMapperImpl implements SysAttachmentMapper {
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    private static final String NAMESPACE = "com.hrp.auth.mapper.SysAttachmentMapper";

    @Override
    public SysAttachment selectById(Long id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }

    @Override
    public List<SysAttachment> selectByBusiness(String businessId) {
        Map<String, Object> params = new HashMap<>();
        params.put("businessId", businessId);
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByBusiness", params);
    }

    @Override
    public int insert(SysAttachment attachment) {
        System.out.println("=== SysAttachmentMapperImpl.insert ===");
        System.out.println("attachment: " + attachment);
        if (attachment != null) {
            System.out.println("businessType: " + attachment.getBusinessType());
            System.out.println("businessId: " + attachment.getBusinessId());
            System.out.println("fileName: " + attachment.getFileName());
            System.out.println("filePath: " + attachment.getFilePath());
            System.out.println("fileSize: " + attachment.getFileSize());
            System.out.println("fileType: " + attachment.getFileType());
            System.out.println("uploadUser: " + attachment.getUploadUser());
            System.out.println("uploadTime: " + attachment.getUploadTime());
        }
        
        try {
            int result = sqlSessionTemplate.insert(NAMESPACE + ".insert", attachment);
            System.out.println("insert 执行结果: " + result);
            if (attachment != null) {
                System.out.println("插入后的 attachmentId: " + attachment.getAttachmentId());
            }
            System.out.println("=== SysAttachmentMapperImpl.insert 完成 ===");
            return result;
        } catch (Exception e) {
            System.err.println("插入附件记录时发生异常：" + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public int updateById(SysAttachment attachment) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", attachment);
    }

    @Override
    public int updateBusinessIdById(Long attachmentId, String businessId) {
        Map<String, Object> params = new HashMap<>();
        params.put("attachmentId", attachmentId);
        params.put("businessId", businessId);
        return sqlSessionTemplate.update(NAMESPACE + ".updateBusinessIdById", params);
    }

    @Override
    public int updateBusinessIdByTypeAndNull(String businessType, String businessId, List<Long> attachmentIds) {
        Map<String, Object> params = new HashMap<>();
        params.put("businessType", businessType);
        params.put("businessId", businessId);
        params.put("attachmentIds", attachmentIds);
        return sqlSessionTemplate.update(NAMESPACE + ".updateBusinessIdByTypeAndNull", params);
    }

    @Override
    public int deleteById(Long id) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", id);
    }

    @Override
    public int deleteByBusiness(String businessId) {
        Map<String, Object> params = new HashMap<>();
        params.put("businessId", businessId);
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteByBusiness", params);
    }
}

