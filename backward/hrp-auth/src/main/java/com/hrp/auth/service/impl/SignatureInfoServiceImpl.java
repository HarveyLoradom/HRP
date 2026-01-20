package com.hrp.auth.service.impl;

import com.hrp.auth.mapper.SignatureInfoMapper;
import com.hrp.auth.service.SignatureInfoService;
import com.hrp.common.entity.SignatureInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class SignatureInfoServiceImpl implements SignatureInfoService {

    @Autowired
    private SignatureInfoMapper signatureInfoMapper;

    @Override
    public SignatureInfo getById(Long id) {
        return signatureInfoMapper.selectById(id);
    }

    @Override
    public SignatureInfo getByEmpId(Long empId) {
        return signatureInfoMapper.selectByEmpId(empId);
    }

    @Override
    @Transactional
    public boolean save(SignatureInfo signatureInfo) {
        signatureInfo.setCreateTime(LocalDateTime.now());
        signatureInfo.setUpdateTime(LocalDateTime.now());
        return signatureInfoMapper.insert(signatureInfo) > 0;
    }

    @Override
    @Transactional
    public boolean update(SignatureInfo signatureInfo) {
        signatureInfo.setUpdateTime(LocalDateTime.now());
        return signatureInfoMapper.updateById(signatureInfo) > 0;
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        return signatureInfoMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional
    public boolean deleteByEmpId(Long empId) {
        return signatureInfoMapper.deleteByEmpId(empId) > 0;
    }

    /**
     * 将 Base64 字符串解码为字节数组，并处理透明背景（如果需要）
     */
    public byte[] decodeBase64Image(String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            return null;
        }
        
        // 移除 data:image/png;base64, 前缀（如果存在）
        String base64Data = base64String;
        if (base64String.contains(",")) {
            base64Data = base64String.substring(base64String.indexOf(",") + 1);
        }
        
        try {
            // 解码 Base64 字符串
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);
            
            // TODO: 如果需要处理透明背景（例如转换为白色背景），可以在这里添加图像处理逻辑
            // 可以使用 Java 的图像处理库（如 BufferedImage）来处理
            
            return imageBytes;
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Base64 解码失败：" + e.getMessage(), e);
        }
    }
}

