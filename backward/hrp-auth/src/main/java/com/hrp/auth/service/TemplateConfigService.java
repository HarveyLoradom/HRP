package com.hrp.auth.service;

import com.hrp.common.entity.TemplateConfig;

import java.util.List;

/**
 * 模板设置服务接口
 */
public interface TemplateConfigService {
    /**
     * 根据ID查询模板设置
     */
    TemplateConfig getById(Long configId);

    /**
     * 根据业务类型和业务类型值查询模板设置
     */
    TemplateConfig getByBusinessType(String businessType, String businessTypeValue);

    /**
     * 根据业务类型查询模板设置列表
     */
    List<TemplateConfig> getByBusinessTypeOnly(String businessType);

    /**
     * 查询所有模板设置
     * @param isActive 启用状态：0-停用，1-启用，null-仅启用
     */
    List<TemplateConfig> getAll(Integer isActive);

    /**
     * 分页查询所有模板设置
     * @param isActive 启用状态：0-停用，1-启用，null-仅启用
     */
    com.hrp.common.entity.PageResult<TemplateConfig> getAllPage(String businessType,Integer isActive, Long page, Long size);

    /**
     * 新增模板设置
     */
    boolean save(TemplateConfig config);

    /**
     * 更新模板设置
     */
    boolean update(TemplateConfig config);

    /**
     * 删除模板设置
     */
    boolean delete(Long configId);
}

