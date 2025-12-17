package com.hrp.auth.service.impl;

import com.hrp.auth.mapper.PrintTemplateMapper;
import com.hrp.auth.service.PrintTemplateService;
import com.hrp.common.entity.PrintTemplate;
import com.hrp.common.dto.TableFieldInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 打印模板服务实现类
 */
@Service
public class PrintTemplateServiceImpl implements PrintTemplateService {

    @Autowired
    private PrintTemplateMapper printTemplateMapper;


    @Autowired
    private DataSource dataSource;

    @Override
    public PrintTemplate getById(Long templateId) {
        return printTemplateMapper.selectById(templateId);
    }

    @Override
    public PrintTemplate getByCode(String templateCode) {
        return printTemplateMapper.selectByCode(templateCode);
    }

    @Override
    public List<PrintTemplate> getByType(String templateType) {
        return printTemplateMapper.selectByType(templateType);
    }

    @Override
    public List<PrintTemplate> getAll() {
        return printTemplateMapper.selectAll();
    }

    @Override
    public PrintTemplate getDefaultByType(String templateType) {
        PrintTemplate template = printTemplateMapper.selectDefaultByType(templateType);
        if (template == null) {
            // 如果没有默认模板，返回第一个启用的模板
            List<PrintTemplate> templates = printTemplateMapper.selectByType(templateType);
            if (templates != null && !templates.isEmpty()) {
                return templates.get(0);
            }
        }
        return template;
    }

    @Override
    public boolean save(PrintTemplate template) {
        // 如果设置为默认模板，需要取消同类型其他模板的默认状态
        if (template.getIsDefault() != null && template.getIsDefault() == 1) {
            List<PrintTemplate> existingTemplates = printTemplateMapper.selectByType(template.getTemplateType());
            for (PrintTemplate existing : existingTemplates) {
                if (existing.getIsDefault() != null && existing.getIsDefault() == 1) {
                    existing.setIsDefault(0);
                    printTemplateMapper.updateById(existing);
                }
            }
        }
        return printTemplateMapper.insert(template) > 0;
    }

    @Override
    public boolean update(PrintTemplate template) {
        // 如果设置为默认模板，需要取消同类型其他模板的默认状态
        if (template.getIsDefault() != null && template.getIsDefault() == 1) {
            List<PrintTemplate> existingTemplates = printTemplateMapper.selectByType(template.getTemplateType());
            for (PrintTemplate existing : existingTemplates) {
                if (!existing.getTemplateId().equals(template.getTemplateId()) 
                    && existing.getIsDefault() != null && existing.getIsDefault() == 1) {
                    existing.setIsDefault(0);
                    printTemplateMapper.updateById(existing);
                }
            }
        }
        return printTemplateMapper.updateById(template) > 0;
    }

    @Override
    public boolean delete(Long templateId) {
        return printTemplateMapper.deleteById(templateId) > 0;
    }

    @Override
    public List<TableFieldInfo> getTableFields(String templateType) {
        List<TableFieldInfo> fields = new ArrayList<>();
        
        // 根据模板类型定义对应的数据库表和字段
        // 这里可以根据实际业务需求配置
        if ("APPLY".equals(templateType) || "PAYOUT".equals(templateType)) {
            // 申请单/报账单字段
            addTableFields(fields, "ctrl_payout", "报账单", new String[][]{
                {"payout_id", "报账单ID", "BIGINT"},
                {"payout_billcode", "报账单号", "VARCHAR"},
                {"emp_id", "职工ID", "BIGINT"},
                {"emp_name", "申请人", "VARCHAR"},
                {"payout_type_id", "报账类型ID", "BIGINT"},
                {"apply_amount", "申请金额", "DECIMAL"},
                {"apply_date", "申请日期", "DATETIME"},
                {"apply_reason", "申请事由", "VARCHAR"},
                {"status", "状态", "VARCHAR"},
                {"remark", "备注", "VARCHAR"},
                {"create_time", "创建时间", "DATETIME"}
            });
        } else if ("CONTRACT".equals(templateType)) {
            // 合同字段
            addTableFields(fields, "pact_main", "合同主表", new String[][]{
                {"pact_id", "合同ID", "BIGINT"},
                {"pact_no", "合同编号", "VARCHAR"},
                {"pact_name", "合同名称", "VARCHAR"},
                {"pact_type", "合同类型", "VARCHAR"},
                {"party_a", "甲方", "VARCHAR"},
                {"party_b", "乙方", "VARCHAR"},
                {"pact_amount", "合同金额", "DECIMAL"},
                {"start_date", "开始日期", "DATE"},
                {"end_date", "结束日期", "DATE"},
                {"status", "状态", "VARCHAR"}
            });
        } else if ("ASSET".equals(templateType)) {
            // 资产审批字段
            addTableFields(fields, "fixed_asset", "固定资产", new String[][]{
                {"asset_id", "资产ID", "BIGINT"},
                {"asset_code", "资产编码", "VARCHAR"},
                {"asset_name", "资产名称", "VARCHAR"},
                {"asset_type", "资产类型", "VARCHAR"},
                {"purchase_amount", "采购金额", "DECIMAL"},
                {"purchase_date", "采购日期", "DATE"},
                {"dept_id", "部门ID", "BIGINT"},
                {"dept_name", "部门名称", "VARCHAR"}
            });
        } else if ("PROCUREMENT".equals(templateType)) {
            // 采购审批字段
            addTableFields(fields, "procurement_requirement", "采购需求", new String[][]{
                {"requirement_id", "需求ID", "BIGINT"},
                {"requirement_no", "需求单号", "VARCHAR"},
                {"requirement_name", "需求名称", "VARCHAR"},
                {"total_amount", "总金额", "DECIMAL"},
                {"apply_date", "申请日期", "DATE"},
                {"status", "状态", "VARCHAR"}
            });
        }
        
        // 添加通用字段（职工表、部门表等）
        addTableFields(fields, "sys_emp", "职工表", new String[][]{
            {"emp_id", "职工ID", "BIGINT"},
            {"emp_code", "工号", "VARCHAR"},
            {"emp_name", "姓名", "VARCHAR"},
            {"emp_sex", "性别", "BIGINT"},
            {"dept_id", "部门ID", "BIGINT"},
            {"dept_name", "部门名称", "VARCHAR"},
            {"emp_phone", "手机号", "VARCHAR"},
            {"emp_email", "邮箱", "VARCHAR"}
        });
        
        addTableFields(fields, "sys_dept", "部门表", new String[][]{
            {"dept_id", "部门ID", "BIGINT"},
            {"dept_code", "部门编码", "VARCHAR"},
            {"dept_name", "部门名称", "VARCHAR"},
            {"dept_phone", "部门电话", "VARCHAR"}
        });
        
        return fields;
    }

    @Override
    public List<TableFieldInfo> getTableFieldsByTableName(String tableName) {
        List<TableFieldInfo> fields = new ArrayList<>();
        
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new RuntimeException("表名不能为空");
        }
        
        // 去除表名中的反引号和空格
        String cleanTableName = tableName.trim().replace("`", "");
        
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String catalog = connection.getCatalog();
            String schema = connection.getSchema();
            
            // 先检查表是否存在
            String actualTableName = null;
            ResultSet tables = metaData.getTables(catalog, schema, null, new String[]{"TABLE"});
            while (tables.next()) {
                String dbTableName = tables.getString("TABLE_NAME");
                if (dbTableName.equalsIgnoreCase(cleanTableName)) {
                    actualTableName = dbTableName;
                    break;
                }
            }
            tables.close();
            
            if (actualTableName == null) {
                throw new RuntimeException("表 '" + tableName + "' 不存在于数据库中");
            }
            
            // 查询表的字段
            ResultSet columns = metaData.getColumns(catalog, schema, actualTableName, null);
            
            while (columns.next()) {
                TableFieldInfo field = new TableFieldInfo();
                field.setTableName(actualTableName);
                field.setTableLabel(actualTableName); // 使用表名作为标签
                field.setColumnName(columns.getString("COLUMN_NAME"));
                field.setColumnLabel(columns.getString("COLUMN_NAME")); // 默认使用列名作为标签
                field.setDataType(columns.getString("TYPE_NAME"));
                
                // 生成字段键：表名.列名
                field.setFieldKey(actualTableName + "." + columns.getString("COLUMN_NAME"));
                
                fields.add(field);
            }
            columns.close();
            
            if (fields.isEmpty()) {
                throw new RuntimeException("表 '" + tableName + "' 没有找到任何字段");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("查询表字段失败：" + e.getMessage() + " (SQL State: " + e.getSQLState() + ")", e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("查询表字段失败：" + e.getMessage(), e);
        }
        
        return fields;
    }
    
    @Override
    public List<String> getAllTableNames() {
        List<String> tableNames = new ArrayList<>();
        
        // 需要过滤的系统表前缀（Flowable工作流引擎的表）
        List<String> excludePrefixes = Arrays.asList(
            "act_",           // Flowable/Activiti 工作流引擎表
            "flw_",           // Flowable 其他表
            "DATABASECHANGELOG",  // Liquibase 表
            "DATABASECHANGELOGLOCK" // Liquibase 锁表
        );
        
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String catalog = connection.getCatalog();
            String schema = connection.getSchema();
            
            // 查询所有表
            ResultSet tables = metaData.getTables(catalog, schema, null, new String[]{"TABLE"});
            
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                
                // 过滤系统表
                boolean isSystemTable = false;
                String upperTableName = tableName.toUpperCase();
                for (String prefix : excludePrefixes) {
                    if (upperTableName.startsWith(prefix.toUpperCase())) {
                        isSystemTable = true;
                        break;
                    }
                }
                
                // 只添加业务表
                if (!isSystemTable) {
                    tableNames.add(tableName);
                }
            }
            tables.close();
            
            // 按表名排序
            tableNames.sort(String::compareToIgnoreCase);
            
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("获取表列表失败：" + e.getMessage(), e);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("获取表列表失败：" + e.getMessage(), e);
        }
        
        return tableNames;
    }
    
    private void addTableFields(List<TableFieldInfo> fields, 
                                 String tableName, String tableLabel, String[][] columns) {
        for (String[] column : columns) {
            TableFieldInfo field = new TableFieldInfo();
            field.setTableName(tableName);
            field.setTableLabel(tableLabel);
            field.setColumnName(column[0]);
            field.setColumnLabel(column[1]);
            field.setDataType(column[2]);
            field.setFieldKey(tableName + "." + column[0]);
            fields.add(field);
        }
    }

    @Override
    public String generatePrintContent(Long templateId, String businessKey, String templateType) {
        try {
            PrintTemplate template = printTemplateMapper.selectById(templateId);
            if (template == null) {
                throw new RuntimeException("打印模板不存在，模板ID：" + templateId);
            }

            // 查询业务数据
            Map<String, Object> sourceData = printTemplateMapper.selectDataByBusinessKey(templateType, businessKey);
            if (sourceData == null || sourceData.isEmpty()) {
                throw new RuntimeException("未找到业务数据，业务主键：" + businessKey);
            }

            // 获取模板内容（优先使用templateContent，如果没有则使用templateJson）
            String templateContent = template.getTemplateContent();
            if (templateContent == null || templateContent.isEmpty()) {
                templateContent = template.getTemplateJson();
            }

            if (templateContent == null || templateContent.isEmpty()) {
                throw new RuntimeException("模板内容为空，无法生成打印内容");
            }

            // 替换模板中的占位符
            String html = templateContent;
            for (Map.Entry<String, Object> entry : sourceData.entrySet()) {
                String placeholder = "${" + entry.getKey() + "}";
                String value = entry.getValue() != null ? entry.getValue().toString() : "";
                html = html.replace(placeholder, value);
            }

            return html;
        } catch (Exception e) {
            throw new RuntimeException("生成打印内容失败：" + e.getMessage(), e);
        }
    }

    @Override
    public String previewPrintTemplate(Long templateId, String businessKey, String templateType) {
        try {
            PrintTemplate template = printTemplateMapper.selectById(templateId);
            if (template == null) {
                throw new RuntimeException("打印模板不存在，模板ID：" + templateId);
            }

            // 查询业务数据
            Map<String, Object> sourceData = null;
            try {
                sourceData = printTemplateMapper.selectDataByBusinessKey(templateType, businessKey);
            } catch (Exception e) {
                // 查询业务数据失败不影响预览，使用测试数据
                System.err.println("查询业务数据失败，使用测试数据：" + e.getMessage());
            }

            // 如果找不到业务数据，生成测试数据
            if (sourceData == null || sourceData.isEmpty()) {
                sourceData = new HashMap<>();
                // 从模板字段配置中提取字段并生成测试数据
                String templateFields = template.getTemplateFields();
                if (templateFields != null && !templateFields.isEmpty()) {
                    // 这里可以根据templateFields生成测试数据
                    // 暂时使用通用测试数据
                    sourceData.put("testField", "测试数据");
                } else {
                    sourceData.put("testField", "测试数据");
                }
            }

            // 获取模板内容
            String templateContent = template.getTemplateContent();
            if (templateContent == null || templateContent.isEmpty()) {
                templateContent = template.getTemplateJson();
            }

            if (templateContent == null || templateContent.isEmpty()) {
                throw new RuntimeException("模板内容为空，无法生成预览");
            }

            // 替换模板中的占位符，null值替换为"测试数据"
            String html = templateContent;
            for (Map.Entry<String, Object> entry : sourceData.entrySet()) {
                String placeholder = "${" + entry.getKey() + "}";
                Object value = entry.getValue();
                if (value == null) {
                    value = "测试数据";
                }
                html = html.replace(placeholder, value.toString());
            }

            return html;
        } catch (Exception e) {
            throw new RuntimeException("预览失败：" + e.getMessage(), e);
        }
    }

    @Override
    public String exportTemplate(PrintTemplate template) {
        try {
            // 构建导出JSON对象
            Map<String, Object> exportData = new HashMap<>();
            exportData.put("templateCode", template.getTemplateCode());
            exportData.put("templateName", template.getTemplateName());
            exportData.put("templateType", template.getTemplateType());
            exportData.put("templateJson", template.getTemplateJson());
            exportData.put("templateFields", template.getTemplateFields());
            exportData.put("pageSize", template.getPageSize());
            exportData.put("orientation", template.getOrientation());
            exportData.put("marginTop", template.getMarginTop());
            exportData.put("marginBottom", template.getMarginBottom());
            exportData.put("marginLeft", template.getMarginLeft());
            exportData.put("marginRight", template.getMarginRight());
            exportData.put("customCss", template.getCustomCss());
            exportData.put("headerHtml", template.getHeaderHtml());
            exportData.put("footerHtml", template.getFooterHtml());
            exportData.put("remark", template.getRemark());
            exportData.put("exportTime", java.time.LocalDateTime.now().toString());
            exportData.put("version", "1.0");
            
            // 转换为JSON字符串
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(exportData);
        } catch (Exception e) {
            throw new RuntimeException("导出模板失败：" + e.getMessage(), e);
        }
    }

    @Override
    public PrintTemplate importTemplate(String jsonContent, String templateCode, String templateName, String templateType) {
        try {
            // 解析JSON内容
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonContent);
            
            // 创建新模板对象
            PrintTemplate template = new PrintTemplate();
            
            // 从JSON中读取或使用传入的参数
            template.setTemplateCode(templateCode != null ? templateCode : 
                (jsonNode.has("templateCode") ? jsonNode.get("templateCode").asText() : null));
            template.setTemplateName(templateName != null ? templateName : 
                (jsonNode.has("templateName") ? jsonNode.get("templateName").asText() : null));
            template.setTemplateType(templateType != null ? templateType : 
                (jsonNode.has("templateType") ? jsonNode.get("templateType").asText() : "CUSTOM"));
            
            // 检查编码是否已存在
            if (template.getTemplateCode() != null) {
                PrintTemplate existTemplate = printTemplateMapper.selectByCode(template.getTemplateCode());
                if (existTemplate != null) {
                    // 如果已存在，生成新的编码
                    template.setTemplateCode(template.getTemplateCode() + "_" + System.currentTimeMillis());
                }
            } else {
                // 如果没有编码，生成一个
                template.setTemplateCode("TEMPLATE_" + System.currentTimeMillis());
            }
            
            // 如果没有名称，使用编码作为名称
            if (template.getTemplateName() == null || template.getTemplateName().isEmpty()) {
                template.setTemplateName(template.getTemplateCode());
            }
            
            // 从JSON中读取模板内容
            if (jsonNode.has("templateJson")) {
                template.setTemplateJson(jsonNode.get("templateJson").asText());
            }
            if (jsonNode.has("templateFields")) {
                template.setTemplateFields(jsonNode.get("templateFields").asText());
            }
            if (jsonNode.has("pageSize")) {
                template.setPageSize(jsonNode.get("pageSize").asText());
            } else {
                template.setPageSize("A4");
            }
            if (jsonNode.has("orientation")) {
                template.setOrientation(jsonNode.get("orientation").asText());
            } else {
                template.setOrientation("portrait");
            }
            if (jsonNode.has("marginTop")) {
                template.setMarginTop(jsonNode.get("marginTop").asInt());
            } else {
                template.setMarginTop(20);
            }
            if (jsonNode.has("marginBottom")) {
                template.setMarginBottom(jsonNode.get("marginBottom").asInt());
            } else {
                template.setMarginBottom(20);
            }
            if (jsonNode.has("marginLeft")) {
                template.setMarginLeft(jsonNode.get("marginLeft").asInt());
            } else {
                template.setMarginLeft(20);
            }
            if (jsonNode.has("marginRight")) {
                template.setMarginRight(jsonNode.get("marginRight").asInt());
            } else {
                template.setMarginRight(20);
            }
            if (jsonNode.has("customCss")) {
                template.setCustomCss(jsonNode.get("customCss").asText());
            }
            if (jsonNode.has("headerHtml")) {
                template.setHeaderHtml(jsonNode.get("headerHtml").asText());
            }
            if (jsonNode.has("footerHtml")) {
                template.setFooterHtml(jsonNode.get("footerHtml").asText());
            }
            if (jsonNode.has("remark")) {
                template.setRemark(jsonNode.get("remark").asText());
            }
            
            // 设置默认值
            template.setIsDefault(0);
            template.setIsActive(1);
            
            // 保存模板
            boolean success = printTemplateMapper.insert(template) > 0;
            if (success) {
                return template;
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("导入模板失败：" + e.getMessage(), e);
        }
    }
}

