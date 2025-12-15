package com.hrp.auth.service;

import com.hrp.common.entity.PrintTemplate;
import com.hrp.common.dto.TableFieldInfo;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 打印模板服务接口
 * 包含模板管理和报表生成功能
 */
public interface PrintTemplateService {
    /**
     * 根据ID查询打印模板
     */
    PrintTemplate getById(Long templateId);

    /**
     * 根据编码查询打印模板
     */
    PrintTemplate getByCode(String templateCode);

    /**
     * 根据类型查询打印模板列表
     */
    List<PrintTemplate> getByType(String templateType);

    /**
     * 查询所有打印模板
     */
    List<PrintTemplate> getAll();

    /**
     * 获取默认模板
     */
    PrintTemplate getDefaultByType(String templateType);

    /**
     * 新增打印模板
     */
    boolean save(PrintTemplate template);

    /**
     * 更新打印模板
     */
    boolean update(PrintTemplate template);

    /**
     * 删除打印模板
     */
    boolean delete(Long templateId);

    /**
     * 根据模板类型获取数据库字段列表
     */
    List<TableFieldInfo> getTableFields(String templateType);

    /**
     * 根据表名获取数据库字段列表
     */
    List<TableFieldInfo> getTableFieldsByTableName(String tableName);
    
    /**
     * 获取数据库中所有表名列表
     */
    List<String> getAllTableNames();

    /**
     * 生成打印内容（返回HTML格式）
     * 
     * @param templateId 模板ID
     * @param businessKey 业务主键
     * @param templateType 模板类型
     * @return HTML打印内容
     */
    String generatePrintContent(Long templateId, String businessKey, String templateType);

    /**
     * 预览打印模板（返回HTML格式）
     * 
     * @param templateId 模板ID
     * @param businessKey 业务主键
     * @param templateType 模板类型
     * @return HTML预览内容
     */
    String previewPrintTemplate(Long templateId, String businessKey, String templateType);

    /**
     * 导出模板为JSON格式
     * 
     * @param template 模板对象
     * @return JSON格式的模板内容
     */
    String exportTemplate(PrintTemplate template);

    /**
     * 导入模板
     * 
     * @param jsonContent JSON格式的模板内容
     * @param templateCode 模板编码（可选，如果为空则从JSON中读取）
     * @param templateName 模板名称（可选，如果为空则从JSON中读取）
     * @param templateType 模板类型（可选，如果为空则从JSON中读取）
     * @return 导入后的模板对象
     */
    PrintTemplate importTemplate(String jsonContent, String templateCode, String templateName, String templateType);
}

