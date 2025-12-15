package com.hrp.auth.service.impl;

import com.hrp.auth.mapper.TemplateConfigMapper;
import com.hrp.auth.service.TemplateConfigService;
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
    public List<TemplateConfig> getAll() {
        return templateConfigMapper.selectAll();
    }

    @Override
    public com.hrp.common.entity.PageResult<TemplateConfig> getAllPage(Long page, Long size) {
        Long offset = (page - 1) * size;
        List<TemplateConfig> records = templateConfigMapper.selectAllPage(offset, size);
        Long total = templateConfigMapper.countAll();
        return com.hrp.common.entity.PageResult.of(records, total, size, page);
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

