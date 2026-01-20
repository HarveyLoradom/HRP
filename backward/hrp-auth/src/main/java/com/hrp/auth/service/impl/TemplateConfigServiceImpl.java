package com.hrp.auth.service.impl;

import com.hrp.auth.mapper.TemplateConfigMapper;
import com.hrp.auth.service.TemplateConfigService;
import com.hrp.common.entity.PageResult;
import com.hrp.common.entity.TemplateConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 模板设置服务实现类
 */
@Service
public class TemplateConfigServiceImpl implements TemplateConfigService {

    @Autowired
    private TemplateConfigMapper templateConfigMapper;

    @Override
    public TemplateConfig getById(Long configId) {
        return templateConfigMapper.selectById(configId);
    }

    @Override
    public TemplateConfig getByBusinessType(String businessType, String businessTypeValue) {
        return templateConfigMapper.selectByBusinessType(businessType, businessTypeValue);
    }

    @Override
    public List<TemplateConfig> getByBusinessTypeOnly(String businessType) {
        return templateConfigMapper.selectByBusinessTypeOnly(businessType);
    }

    @Override
    public List<TemplateConfig> getAll(Integer isActive) {
        return templateConfigMapper.selectAll(isActive);
    }

    @Override
    public PageResult<TemplateConfig> getAllPage(String businessType,Integer isActive, Long page, Long size) {
        if (page == null || page < 1) {
            page = 1L;
        }
        if (size == null || size < 1) {
            size = 10L;
        }
        Long offset = (page - 1) * size;
        List<TemplateConfig> list = templateConfigMapper.selectAllPage(businessType,isActive, offset, size);
        Long total = templateConfigMapper.countAll(businessType, isActive);
        return new PageResult<>(list, total, size, page);
    }

    @Override
    @Transactional
    public boolean save(TemplateConfig config) {
        if (config.getIsActive() == null) {
            config.setIsActive(1);
        }
        return templateConfigMapper.insert(config) > 0;
    }

    @Override
    @Transactional
    public boolean update(TemplateConfig config) {
        return templateConfigMapper.updateById(config) > 0;
    }

    @Override
    @Transactional
    public boolean delete(Long configId) {
        return templateConfigMapper.deleteById(configId) > 0;
    }
}

