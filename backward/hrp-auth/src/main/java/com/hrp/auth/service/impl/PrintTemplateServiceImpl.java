package com.hrp.auth.service.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrp.auth.mapper.PrintTemplateMapper;
import com.hrp.auth.mapper.ProcessTaskMapper;
import com.hrp.auth.service.PrintTemplateService;
import com.hrp.common.dto.TableFieldInfo;
import com.hrp.common.entity.PrintTemplate;
import com.hrp.common.entity.ProcessTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * 打印模板服务实现类
 */
@Service
public class PrintTemplateServiceImpl implements PrintTemplateService {

    @Autowired
    private PrintTemplateMapper printTemplateMapper;

    @Autowired(required = false)
    private ProcessTaskMapper processTaskMapper;

    @Autowired
    private DataSource dataSource;
    
    @Autowired(required = false)
    private com.hrp.auth.service.TemplateConfigService templateConfigService;
    
    @Autowired(required = false)
    private com.hrp.auth.feign.BudgServiceClient budgServiceClient;
    
    @Autowired(required = false)
    private com.hrp.auth.feign.ReimbServiceClient reimbServiceClient;

    @Autowired(required = false)
    private com.hrp.auth.feign.AssetServiceClient assetServiceClient;
    
    @Autowired(required = false)
    private com.hrp.auth.feign.HrServiceClient hrServiceClient;
    
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public PrintTemplate getById(Long templateId) {
        return printTemplateMapper.selectById(templateId);
    }

    @Override
    public PrintTemplate getByCode(String templateCode) {
        return printTemplateMapper.selectByCode(templateCode);
    }

    @Override
    public List<PrintTemplate> getByType(String templateType, Integer isActive) {
        return printTemplateMapper.selectByType(templateType, isActive);
    }

    @Override
    public List<PrintTemplate> getAll(Integer isActive) {
        return printTemplateMapper.selectAll(isActive);
    }

    @Override
    public PrintTemplate getDefaultByType(String templateType) {
        PrintTemplate template = printTemplateMapper.selectDefaultByType(templateType);
        if (template == null) {
            // 如果没有默认模板，返回第一个启用的模板
            List<PrintTemplate> templates = printTemplateMapper.selectByType(templateType, 1);
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
            // 查询所有模板（包括停用的），因为需要取消默认状态
            List<PrintTemplate> existingTemplates = printTemplateMapper.selectByType(template.getTemplateType(), null);
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
        // 如果模板编码被修改，检查编码唯一性（排除当前记录）
        if (template.getTemplateCode() != null && !template.getTemplateCode().isEmpty()) {
            PrintTemplate existTemplate = printTemplateMapper.selectByCode(template.getTemplateCode());
            if (existTemplate != null && !existTemplate.getTemplateId().equals(template.getTemplateId())) {
                throw new RuntimeException("模板编码已存在：" + template.getTemplateCode());
            }
        }
        
        // 如果设置为默认模板，需要取消同类型其他模板的默认状态
        if (template.getIsDefault() != null && template.getIsDefault() == 1) {
            // 查询所有模板（包括停用的），因为需要取消默认状态
            List<PrintTemplate> existingTemplates = printTemplateMapper.selectByType(template.getTemplateType(), null);
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

            // 获取模板内容（只使用templateJson）
            String templateJson = template.getTemplateJson();

            // 检查模板内容是否为空
            if (templateJson == null || templateJson.trim().isEmpty()) {
                throw new RuntimeException("模板内容为空，无法生成打印内容");
            }

            // 从模板设置表获取业务类型（通过businessKey获取templateConfigId，然后获取business_type）
            String actualBusinessType = getBusinessTypeFromTemplateConfig(businessKey);
            if (actualBusinessType == null || actualBusinessType.isEmpty()) {
                // 如果无法从模板设置表获取，使用备用方法推断
                actualBusinessType = inferTemplateTypeFromBusinessKey(businessKey, null);
            }
            System.out.println("根据businessKey推断的业务类型: " + actualBusinessType + "（businessKey: " + businessKey + "）");

            // 先查询数据
            Map<String, Object> sourceData = new HashMap<>();
            
            // 提取所有占位符
            java.util.Set<String> dataSources = new java.util.HashSet<>();
            java.util.Set<String> allPlaceholders = new java.util.HashSet<>();
            
            // 从JSON中提取占位符
            if (templateJson != null && !templateJson.trim().isEmpty()) {
                // 如果templateContent为空但templateJson不为空，从JSON中提取占位符
                try {
                    JsonNode rootNode = objectMapper.readTree(templateJson);
                    JsonNode elementsNode = rootNode.get("elements");
                    if (elementsNode != null && elementsNode.isArray()) {
                        for (JsonNode elementNode : elementsNode) {
                            // 从label-field类型的元素中提取fieldKey
                            if (elementNode.has("type") && "label-field".equals(elementNode.get("type").asText())) {
                                if (elementNode.has("fieldKey")) {
                                    String fieldKey = elementNode.get("fieldKey").asText();
                                    allPlaceholders.add(fieldKey);
                                    if (fieldKey.contains(".")) {
                                        String dataSource = fieldKey.substring(0, fieldKey.indexOf("."));
                                        dataSources.add(dataSource);
                                    } else {
                                        dataSources.add(getDefaultDataSource(actualBusinessType));
            }
                                }
                            }
                            // 从table类型的元素中提取columns的fieldKey
                            if (elementNode.has("type") && "table".equals(elementNode.get("type").asText())) {
                                JsonNode columnsNode = elementNode.get("columns");
                                if (columnsNode != null && columnsNode.isArray()) {
                                    for (JsonNode columnNode : columnsNode) {
                                        if (columnNode.has("fieldKey")) {
                                            String fieldKey = columnNode.get("fieldKey").asText();
                                            allPlaceholders.add(fieldKey);
                                            if (fieldKey.contains(".")) {
                                                String dataSource = fieldKey.substring(0, fieldKey.indexOf("."));
                                                dataSources.add(dataSource);
                                            } else {
                                                dataSources.add(getDefaultDataSource(actualBusinessType));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("从JSON模板中提取占位符失败: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            // 如果dataSources为空（模板中没有占位符或解析失败），使用默认查询
            if (dataSources.isEmpty()) {
                System.out.println("模板中没有占位符或解析失败，使用默认查询，businessKey=" + businessKey);
                // 使用推断的业务类型查询对应的业务数据
                try {
                    Map<String, Object> defaultData = printTemplateMapper.selectDataByBusinessKey(actualBusinessType, businessKey);
                    if (defaultData != null && !defaultData.isEmpty()) {
                        System.out.println("默认查询成功，返回 " + defaultData.size() + " 个字段");
                        sourceData.putAll(defaultData);
                    } else {
                        System.err.println("默认查询返回空数据，businessType=" + actualBusinessType + ", businessKey=" + businessKey);
                        // 如果默认查询返回空，根据businessType自动推断
                        if (actualBusinessType != null && actualBusinessType.startsWith("BUDGET")) {
                            loadBudgetApplyData(sourceData, actualBusinessType, businessKey);
                        } else if (actualBusinessType != null && (actualBusinessType.startsWith("APPLY") || actualBusinessType.startsWith("PAYOUT"))) {
                            loadPayoutData(sourceData, actualBusinessType, businessKey);
                        } else if (actualBusinessType != null && actualBusinessType.startsWith("CONTRACT")) {
                            loadContractData(sourceData, actualBusinessType, businessKey);
                        } else if (actualBusinessType != null && (actualBusinessType.startsWith("ASSET") || actualBusinessType.equals("ASSET_TYPE"))) {
                            loadAssetPurchaseApplyData(sourceData, actualBusinessType, businessKey);
                        } else if (actualBusinessType != null && (actualBusinessType.startsWith("HR") || actualBusinessType.equals("HR_TYPE"))) {
                            loadHrApplyData(sourceData, actualBusinessType, businessKey);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("默认查询异常: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("解析到数据源: " + dataSources + ", 开始分别查询");
                // 根据数据源分别查询数据
                for (String dataSource : dataSources) {
                    if ("budget_apply".equals(dataSource) || "budg".equals(dataSource)) {
                        loadBudgetApplyData(sourceData, actualBusinessType, businessKey);
                    } else if ("wf_process_task".equals(dataSource) || "process_task".equals(dataSource)) {
                        loadProcessTaskData(sourceData, businessKey);
                    } else if ("ctrl_payout".equals(dataSource) || "payout".equals(dataSource)) {
                        loadPayoutData(sourceData, actualBusinessType, businessKey);
                    } else if ("ctrl_payout_payment".equals(dataSource) || "payment".equals(dataSource)) {
                        loadPayoutPaymentData(sourceData, actualBusinessType, businessKey);
                    } else if ("ctrl_payout_invoice".equals(dataSource) || "invoice".equals(dataSource)) {
                        loadPayoutInvoiceData(sourceData, actualBusinessType, businessKey);
                    } else if ("budget_detail".equals(dataSource) || "budget_detail_record".equals(dataSource)) {
                        loadBudgetDetailData(sourceData, businessKey);
                    } else if ("pact_main".equals(dataSource) || "contract".equals(dataSource)) {
                        // 加载合同数据
                        loadContractData(sourceData, actualBusinessType, businessKey);
                    } else if ("asset_purchase_apply_main".equals(dataSource) || "asset_purchase_apply".equals(dataSource) || "asset".equals(dataSource)) {
                        // 加载资产采购申请数据
                        loadAssetPurchaseApplyData(sourceData, actualBusinessType, businessKey);
                    } else if ("asset_purchase_apply_detail".equals(dataSource) || "asset_detail".equals(dataSource)) {
                        // 加载资产采购申请明细数据
                        loadAssetPurchaseApplyDetailData(sourceData, businessKey);
                    } else if ("hr_apply".equals(dataSource) || "hr".equals(dataSource)) {
                        // 加载HR业务申请数据
                        loadHrApplyData(sourceData, actualBusinessType, businessKey);
                    }
                }
            }
            
            // 如果查询不到数据，抛出异常
            if (sourceData.isEmpty()) {
                System.err.println("所有数据源查询都返回空，businessKey=" + businessKey);
                System.err.println("解析到的数据源: " + dataSources);
                System.err.println("解析到的占位符: " + allPlaceholders);
                System.err.println("推断的业务类型: " + actualBusinessType);
                throw new RuntimeException("未找到业务数据，业务主键：" + businessKey);
            } else {
                System.out.println("成功查询到数据，共 " + sourceData.size() + " 个字段");
            }

            // 生成HTML内容（只使用JSON格式模板）
            String html;
            if (templateJson != null && !templateJson.isEmpty()) {
                System.out.println("检测到JSON格式模板，开始转换为HTML");
                html = convertJsonTemplateToHtml(templateJson, sourceData, template);
            } else {
                throw new RuntimeException("模板内容为空，无法生成打印内容");
            }

            return html;
        } catch (Exception e) {
            throw new RuntimeException("生成打印内容失败：" + e.getMessage(), e);
        }
    }
    
    /**
     * 加载预算申请数据（包括主表和关联表）
     */
    private void loadBudgetApplyData(Map<String, Object> sourceData, String businessType, String businessKey) {
        System.out.println("查询预算申请数据，businessType=" + businessType + ", businessKey=" + businessKey);
        try {
            // 直接使用businessType，XML已支持BUDGET_TYPE、BUDGET、BUDG等格式
            // businessType来自template_config表的business_type字段（如BUDGET_TYPE）
            // selectDataByBusinessKey方法的XML已兼容这些格式
            Map<String, Object> budgetData = printTemplateMapper.selectDataByBusinessKey(businessType, businessKey);
            if (budgetData != null && !budgetData.isEmpty()) {
                System.out.println("预算申请数据查询成功，返回 " + budgetData.size() + " 个字段");
                // 将字段名转换为带前缀的格式，同时支持驼峰和下划线格式
                for (Map.Entry<String, Object> entry : budgetData.entrySet()) {
                    String fieldName = entry.getKey();
                    // 如果字段名是驼峰格式，转换为下划线格式
                    String snakeCase = fieldName.contains("_") ? fieldName : convertCamelToSnake(fieldName);
                    // 添加带前缀的key（如 budget_apply.applicant_name）
                    String keyWithPrefix = "budget_apply." + snakeCase;
                    sourceData.put(keyWithPrefix, entry.getValue());
                    // 同时支持不带前缀的格式（向后兼容）
                    sourceData.put(fieldName, entry.getValue());
                    sourceData.put(snakeCase, entry.getValue());
                    
                    // 根据字段后缀自动添加表前缀映射（如 dept_name -> sys_dept.dept_name）
                    addTablePrefixMapping(sourceData, fieldName, snakeCase, entry.getValue());
                }
                
                // 自动查询关联表：预算明细
                loadBudgetDetailData(sourceData, businessKey);
            } else {
                System.err.println("预算申请数据查询返回空，businessType=" + businessType + ", businessKey=" + businessKey);
            }
        } catch (Exception e) {
            System.err.println("预算申请数据查询异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 加载报账数据（包括主表和关联表）
     */
    private void loadPayoutData(Map<String, Object> sourceData, String businessType, String businessKey) {
        System.out.println("查询报账数据，businessType=" + businessType + ", businessKey=" + businessKey);
        try {
            Map<String, Object> payoutData = printTemplateMapper.selectDataByBusinessKey(businessType, businessKey);
            if (payoutData != null && !payoutData.isEmpty()) {
                System.out.println("报账数据查询成功，返回 " + payoutData.size() + " 个字段");
                // 处理主表数据
                for (Map.Entry<String, Object> entry : payoutData.entrySet()) {
                    String fieldName = entry.getKey();
                    String snakeCase = fieldName.contains("_") ? fieldName : convertCamelToSnake(fieldName);
                    String keyWithPrefix = "ctrl_payout." + snakeCase;
                    sourceData.put(keyWithPrefix, entry.getValue());
                    sourceData.put(fieldName, entry.getValue());
                    sourceData.put(snakeCase, entry.getValue());
                    
                    // 根据字段后缀自动添加表前缀映射（如 dept_name -> sys_dept.dept_name）
                    addTablePrefixMapping(sourceData, fieldName, snakeCase, entry.getValue());
                }
                
                // 自动查询关联表：支付明细、发票信息、预算明细
                Object payoutIdObj = payoutData.get("payoutId");
                if (payoutIdObj != null) {
                    Long payoutId = null;
                    if (payoutIdObj instanceof Long) {
                        payoutId = (Long) payoutIdObj;
                    } else if (payoutIdObj instanceof Number) {
                        payoutId = ((Number) payoutIdObj).longValue();
                    }
                    
                    if (payoutId != null) {
                        // 查询支付明细
                        loadPayoutPaymentDataByPayoutId(sourceData, payoutId);
                        // 查询发票信息
                        loadPayoutInvoiceDataByPayoutId(sourceData, payoutId);
                    }
                }
                
                // 查询预算明细（根据business_no）
                loadBudgetDetailData(sourceData, businessKey);
            }
        } catch (Exception e) {
            System.err.println("报账数据查询异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 加载支付明细数据（根据businessKey，需要先获取payoutId）
     */
    private void loadPayoutPaymentData(Map<String, Object> sourceData, String businessType, String businessKey) {
        System.out.println("查询支付明细数据，businessKey=" + businessKey);
        try {
            // 先查询报账表获取payout_id
            Map<String, Object> payoutData = printTemplateMapper.selectDataByBusinessKey(businessType, businessKey);
            if (payoutData != null && payoutData.containsKey("payoutId")) {
                Object payoutIdObj = payoutData.get("payoutId");
                Long payoutId = null;
                if (payoutIdObj instanceof Long) {
                    payoutId = (Long) payoutIdObj;
                } else if (payoutIdObj instanceof Number) {
                    payoutId = ((Number) payoutIdObj).longValue();
                }
                if (payoutId != null) {
                    loadPayoutPaymentDataByPayoutId(sourceData, payoutId);
                }
            }
        } catch (Exception e) {
            System.err.println("支付明细数据查询异常: " + e.getMessage());
        }
    }

    /**
     * 加载支付明细数据（根据payoutId）
     */
    private void loadPayoutPaymentDataByPayoutId(Map<String, Object> sourceData, Long payoutId) {
        try {
            List<Map<String, Object>> payments = printTemplateMapper.selectPaymentsByPayoutId(payoutId);
            if (payments != null && !payments.isEmpty()) {
                for (int i = 0; i < payments.size(); i++) {
                    Map<String, Object> payment = payments.get(i);
                    for (Map.Entry<String, Object> entry : payment.entrySet()) {
                        String fieldName = entry.getKey();
                        String snakeCase = fieldName.contains("_") ? fieldName : convertCamelToSnake(fieldName);
                        String keyWithPrefix = "ctrl_payout_payment[" + i + "]." + snakeCase;
                        sourceData.put(keyWithPrefix, entry.getValue());
                        if (i == 0) {
                            sourceData.put("ctrl_payout_payment." + snakeCase, entry.getValue());
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("查询支付明细异常: " + e.getMessage());
        }
    }

    /**
     * 加载发票信息数据（根据businessKey，需要先获取payoutId）
     */
    private void loadPayoutInvoiceData(Map<String, Object> sourceData, String businessType, String businessKey) {
        System.out.println("查询发票信息数据，businessKey=" + businessKey);
        try {
            Map<String, Object> payoutData = printTemplateMapper.selectDataByBusinessKey(businessType, businessKey);
            if (payoutData != null && payoutData.containsKey("payoutId")) {
                Object payoutIdObj = payoutData.get("payoutId");
                Long payoutId = null;
                if (payoutIdObj instanceof Long) {
                    payoutId = (Long) payoutIdObj;
                } else if (payoutIdObj instanceof Number) {
                    payoutId = ((Number) payoutIdObj).longValue();
                }
                if (payoutId != null) {
                    loadPayoutInvoiceDataByPayoutId(sourceData, payoutId);
                }
            }
        } catch (Exception e) {
            System.err.println("发票信息数据查询异常: " + e.getMessage());
        }
    }

    /**
     * 加载发票信息数据（根据payoutId）
     */
    private void loadPayoutInvoiceDataByPayoutId(Map<String, Object> sourceData, Long payoutId) {
        try {
            List<Map<String, Object>> invoices = printTemplateMapper.selectInvoicesByPayoutId(payoutId);
            if (invoices != null && !invoices.isEmpty()) {
                for (int i = 0; i < invoices.size(); i++) {
                    Map<String, Object> invoice = invoices.get(i);
                    for (Map.Entry<String, Object> entry : invoice.entrySet()) {
                        String fieldName = entry.getKey();
                        String snakeCase = fieldName.contains("_") ? fieldName : convertCamelToSnake(fieldName);
                        String keyWithPrefix = "ctrl_payout_invoice[" + i + "]." + snakeCase;
                        sourceData.put(keyWithPrefix, entry.getValue());
                        if (i == 0) {
                            sourceData.put("ctrl_payout_invoice." + snakeCase, entry.getValue());
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("查询发票信息异常: " + e.getMessage());
        }
    }

    /**
     * 加载预算明细数据
     */
    private void loadBudgetDetailData(Map<String, Object> sourceData, String businessKey) {
        System.out.println("查询预算明细数据，businessKey=" + businessKey);
        try {
            List<Map<String, Object>> budgetDetails = printTemplateMapper.selectBudgetDetailsByBusinessNo(businessKey);
            if (budgetDetails != null && !budgetDetails.isEmpty()) {
                System.out.println("查询到预算明细数据，共 " + budgetDetails.size() + " 条记录");
                for (int i = 0; i < budgetDetails.size(); i++) {
                    Map<String, Object> detail = budgetDetails.get(i);
                    for (Map.Entry<String, Object> entry : detail.entrySet()) {
                        String fieldName = entry.getKey();
                        String snakeCase = fieldName.contains("_") ? fieldName : convertCamelToSnake(fieldName);
                        // 带索引的格式（用于循环显示多条记录）：budget_detail[0].item_code
                        String keyWithPrefix = "budget_detail[" + i + "]." + snakeCase;
                        sourceData.put(keyWithPrefix, entry.getValue());
                        // 第一条记录也支持不带索引的格式（向后兼容）：budget_detail.item_code
                        if (i == 0) {
                            sourceData.put("budget_detail." + snakeCase, entry.getValue());
                        }
                    }
                }
            } else {
                System.out.println("未查询到预算明细数据，businessKey=" + businessKey);
            }
        } catch (Exception e) {
            System.err.println("查询预算明细异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 加载合同数据（包括主表和关联表）
     */
    private void loadContractData(Map<String, Object> sourceData, String businessType, String businessKey) {
        System.out.println("查询合同数据，businessType=" + businessType + ", businessKey=" + businessKey);
        try {
            Map<String, Object> contractData = printTemplateMapper.selectDataByBusinessKey(businessType, businessKey);
            if (contractData != null && !contractData.isEmpty()) {
                System.out.println("合同数据查询成功，返回 " + contractData.size() + " 个字段");
                // 处理主表数据
                for (Map.Entry<String, Object> entry : contractData.entrySet()) {
                    String fieldName = entry.getKey();
                    String snakeCase = fieldName.contains("_") ? fieldName : convertCamelToSnake(fieldName);
                    String keyWithPrefix = "pact_main." + snakeCase;
                    sourceData.put(keyWithPrefix, entry.getValue());
                    // 同时支持contract前缀（兼容性）
                    String keyWithContractPrefix = "contract." + snakeCase;
                    sourceData.put(keyWithContractPrefix, entry.getValue());
                    // 同时支持不带前缀的格式（向后兼容）
                    sourceData.put(fieldName, entry.getValue());
                    sourceData.put(snakeCase, entry.getValue());
                    
                    // 根据字段后缀自动添加表前缀映射（如 dept_name -> sys_dept.dept_name）
                    addTablePrefixMapping(sourceData, fieldName, snakeCase, entry.getValue());
                }
            } else {
                System.err.println("合同数据查询返回空，businessType=" + businessType + ", businessKey=" + businessKey);
            }
        } catch (Exception e) {
            System.err.println("合同数据查询异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 加载资产采购申请数据（包括主表和关联表）
     */
    private void loadAssetPurchaseApplyData(Map<String, Object> sourceData, String businessType, String businessKey) {
        System.out.println("查询资产采购申请数据，businessType=" + businessType + ", businessKey=" + businessKey);
        try {
            // 直接使用businessType，XML已支持ASSET_TYPE、ASSET、ASSET_PURCHASE_APPLY等格式
            Map<String, Object> assetData = printTemplateMapper.selectDataByBusinessKey(businessType, businessKey);
            if (assetData != null && !assetData.isEmpty()) {
                System.out.println("资产采购申请数据查询成功，返回 " + assetData.size() + " 个字段");
                // 将字段名转换为带前缀的格式，同时支持驼峰和下划线格式
                for (Map.Entry<String, Object> entry : assetData.entrySet()) {
                    String fieldName = entry.getKey();
                    String snakeCase = fieldName.contains("_") ? fieldName : convertCamelToSnake(fieldName);
                    // 添加带前缀的key（如 asset_purchase_apply_main.apply_no）
                    String keyWithPrefix = "asset_purchase_apply_main." + snakeCase;
                    sourceData.put(keyWithPrefix, entry.getValue());
                    // 同时支持不带前缀的格式（向后兼容）
                    sourceData.put(fieldName, entry.getValue());
                    sourceData.put(snakeCase, entry.getValue());
                    
                    // 根据字段后缀自动添加表前缀映射（如 dept_name -> sys_dept.dept_name）
                    addTablePrefixMapping(sourceData, fieldName, snakeCase, entry.getValue());
                }
                
                // 自动查询关联表：资产采购申请明细
                loadAssetPurchaseApplyDetailData(sourceData, businessKey);
            } else {
                System.err.println("资产采购申请数据查询返回空，businessType=" + businessType + ", businessKey=" + businessKey);
            }
        } catch (Exception e) {
            System.err.println("资产采购申请数据查询异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 加载资产采购申请明细数据
     */
    private void loadAssetPurchaseApplyDetailData(Map<String, Object> sourceData, String businessKey) {
        try {
            List<Map<String, Object>> detailList = printTemplateMapper.selectAssetPurchaseApplyDetailsByApplyNo(businessKey);
            if (detailList != null && !detailList.isEmpty()) {
                System.out.println("资产采购申请明细数据查询成功，返回 " + detailList.size() + " 条记录");
                sourceData.put("asset_purchase_apply_detail_list", detailList);
                sourceData.put("detailList", detailList);
                
                // 同时为每条明细添加带前缀的字段（用于循环打印）
                for (int i = 0; i < detailList.size(); i++) {
                    Map<String, Object> detail = detailList.get(i);
                    for (Map.Entry<String, Object> entry : detail.entrySet()) {
                        String fieldName = entry.getKey();
                        String snakeCase = fieldName.contains("_") ? fieldName : convertCamelToSnake(fieldName);
                        // 添加索引前缀（如 detail[0].asset_code）
                        String keyWithIndex = "detail[" + i + "]." + snakeCase;
                        sourceData.put(keyWithIndex, entry.getValue());
                        // 添加表前缀（如 asset_purchase_apply_detail.asset_code）
                        String keyWithTablePrefix = "asset_purchase_apply_detail." + snakeCase;
                        sourceData.put(keyWithTablePrefix + "[" + i + "]", entry.getValue());
                    }
                }
            } else {
                System.out.println("资产采购申请明细数据为空，businessKey=" + businessKey);
            }
        } catch (Exception e) {
            System.err.println("资产采购申请明细数据查询异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 加载HR业务申请数据
     */
    private void loadHrApplyData(Map<String, Object> sourceData, String businessType, String businessKey) {
        System.out.println("查询HR业务申请数据，businessType=" + businessType + ", businessKey=" + businessKey);
        try {
            // 优先使用Mapper查询（如果XML已配置）
            Map<String, Object> hrApplyData = printTemplateMapper.selectDataByBusinessKey(businessType, businessKey);
            if (hrApplyData == null || hrApplyData.isEmpty()) {
                // 如果Mapper查询失败，使用Feign客户端查询
                if (hrServiceClient != null) {
                    com.hrp.common.entity.Result<com.hrp.common.entity.HrApply> result = hrServiceClient.getHrApplyByApplyNo(businessKey);
                    if (result != null && result.getCode() == 200 && result.getData() != null) {
                        com.hrp.common.entity.HrApply hrApply = result.getData();
                        // 将实体对象转换为Map
                        hrApplyData = new HashMap<>();
                        hrApplyData.put("applyId", hrApply.getApplyId());
                        hrApplyData.put("applyNo", hrApply.getApplyNo());
                        hrApplyData.put("empId", hrApply.getEmpId());
                        hrApplyData.put("empCode", hrApply.getEmpCode());
                        hrApplyData.put("empName", hrApply.getEmpName());
                        hrApplyData.put("empPhone", hrApply.getEmpPhone());
                        hrApplyData.put("deptId", hrApply.getDeptId());
                        hrApplyData.put("deptName", hrApply.getDeptName());
                        hrApplyData.put("hrApplyType", hrApply.getHrApplyType());
                        hrApplyData.put("hrApplySubType", hrApply.getHrApplySubType());
                        hrApplyData.put("startTime", hrApply.getStartTime());
                        hrApplyData.put("endTime", hrApply.getEndTime());
                        hrApplyData.put("supplementId", hrApply.getSupplementId());
                        hrApplyData.put("applyDay", hrApply.getApplyDay());
                        hrApplyData.put("applyReason", hrApply.getApplyReason());
                        hrApplyData.put("mainAttachId", hrApply.getMainAttachId());
                        hrApplyData.put("status", hrApply.getStatus());
                        hrApplyData.put("templateConfigId", hrApply.getTemplateConfigId());
                        hrApplyData.put("processDefinitionId", hrApply.getProcessDefinitionId());
                        hrApplyData.put("processInstanceId", hrApply.getProcessInstanceId());
                        hrApplyData.put("remark", hrApply.getRemark());
                        hrApplyData.put("createUser", hrApply.getCreateUser());
                        hrApplyData.put("createTime", hrApply.getCreateTime());
                        hrApplyData.put("updateTime", hrApply.getUpdateTime());
                    }
                }
            }
            
            if (hrApplyData != null && !hrApplyData.isEmpty()) {
                System.out.println("HR业务申请数据查询成功，返回 " + hrApplyData.size() + " 个字段");
                // 处理主表数据
                for (Map.Entry<String, Object> entry : hrApplyData.entrySet()) {
                    String fieldName = entry.getKey();
                    String snakeCase = fieldName.contains("_") ? fieldName : convertCamelToSnake(fieldName);
                    String keyWithPrefix = "hr_apply." + snakeCase;
                    sourceData.put(keyWithPrefix, entry.getValue());
                    // 同时支持不带前缀的格式（向后兼容）
                    sourceData.put(fieldName, entry.getValue());
                    sourceData.put(snakeCase, entry.getValue());
                    
                    // 根据字段后缀自动添加表前缀映射（如 dept_name -> sys_dept.dept_name）
                    addTablePrefixMapping(sourceData, fieldName, snakeCase, entry.getValue());
                }
            } else {
                System.err.println("HR业务申请数据查询返回空，businessType=" + businessType + ", businessKey=" + businessKey);
            }
        } catch (Exception e) {
            System.err.println("HR业务申请数据查询异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 加载流程任务数据（审批记录）
     */
    private void loadProcessTaskData(Map<String, Object> sourceData, String businessKey) {
        if (processTaskMapper == null) {
            return;
        }
        
        try {
            List<ProcessTask> allTasks = processTaskMapper.selectByBusinessKey(businessKey);
            if (allTasks != null && !allTasks.isEmpty()) {
                // 过滤：只获取comment有值的记录，并按printOrder排序
                List<ProcessTask> tasks = new ArrayList<>();
                for (ProcessTask task : allTasks) {
                    if (task.getComment() != null && !task.getComment().trim().isEmpty()) {
                        tasks.add(task);
                    }
                }
                
                // 按printOrder排序（如果printOrder为null，则排在最后）
                tasks.sort((t1, t2) -> {
                    Integer order1 = t1.getPrintOrder();
                    Integer order2 = t2.getPrintOrder();
                    if (order1 == null && order2 == null) {
                        return 0;
                    }
                    if (order1 == null) {
                        return 1; // null排在后面
                    }
                    if (order2 == null) {
                        return -1; // null排在后面
                    }
                    return order1.compareTo(order2);
                });
                
                System.out.println("打印查询流程任务，过滤后共 " + tasks.size() + " 条记录（comment有值）");
                
                // 对于多个任务，使用索引（如 wf_process_task[0].task_name）
                for (int i = 0; i < tasks.size(); i++) {
                    ProcessTask task = tasks.get(i);
                    // 使用反射获取所有字段
                    java.lang.reflect.Field[] fields = task.getClass().getDeclaredFields();
                    for (java.lang.reflect.Field field : fields) {
                        try {
                            field.setAccessible(true);
                            Object value = field.get(task);
                            String fieldName = field.getName();
                            String snakeCase = convertCamelToSnake(fieldName);
                            
                            // 如果是签名字段，转换为HTML img标签
                            if ("approverSignature".equals(fieldName) && value != null) {
                                String signatureBase64 = value.toString();
                                if (!signatureBase64.trim().isEmpty()) {
                                    // 如果已经是data:image格式，直接使用；否则添加前缀
                                    String imgSrc = signatureBase64;
                                    if (!signatureBase64.startsWith("data:image")) {
                                        imgSrc = "data:image/png;base64," + signatureBase64;
                                    }
                                    value = "<img src=\"" + imgSrc + "\" style=\"max-width: 100px; max-height: 50px;\" />";
                                } else {
                                    value = "";
                                }
                            }
                            
                            String key = "wf_process_task[" + i + "]." + snakeCase;
                            sourceData.put(key, value);
                            // 如果是第一个任务，也支持不带索引的格式（向后兼容）
                            if (i == 0) {
                                sourceData.put("wf_process_task." + snakeCase, value);
                            }
                        } catch (Exception e) {
                            // 忽略字段访问错误
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("查询流程任务数据异常: " + e.getMessage());
        }
    }
    
    /**
     * 将驼峰命名转换为下划线命名（如 applicantName -> applicant_name）
     */
    private String convertCamelToSnake(String camelCase) {
        if (camelCase == null || camelCase.isEmpty()) {
            return camelCase;
        }
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
    
    /**
     * 根据字段名后缀自动添加表前缀映射
     * 例如：dept_name -> sys_dept.dept_name, emp_name -> sys_emp.emp_name
     */
    private void addTablePrefixMapping(Map<String, Object> sourceData, String fieldName, String snakeCase, Object value) {
        // 定义字段后缀到表名的映射规则
        String[][] fieldToTableMapping = {
            // 部门相关字段 -> sys_dept
            {"dept_name", "sys_dept"},
            {"dept_code", "sys_dept"},
            {"dept_id", "sys_dept"},
            {"dept_phone", "sys_dept"},
            // 职工相关字段 -> sys_emp
            {"emp_name", "sys_emp"},
            {"emp_code", "sys_emp"},
            {"emp_id", "sys_emp"},
            {"emp_phone", "sys_emp"},
            {"emp_email", "sys_emp"},
            {"emp_sex", "sys_emp"}
        };
        
        // 检查字段名（支持驼峰和下划线格式）
        String fieldToCheck = snakeCase.toLowerCase();
        
        for (String[] mapping : fieldToTableMapping) {
            String suffix = mapping[0];
            String tableName = mapping[1];
            
            // 如果字段名以指定后缀结尾，添加表前缀映射
            if (fieldToCheck.equals(suffix) || fieldToCheck.endsWith("_" + suffix)) {
                String tablePrefixedKey = tableName + "." + suffix;
                sourceData.put(tablePrefixedKey, value);
                break;
            }
        }
    }
    
    /**
     * 根据模板类型获取默认数据源
     */
    private String getDefaultDataSource(String templateType) {
        if ("BUDGET".equals(templateType)) {
            return "budget_apply";
        } else if ("APPLY".equals(templateType) || "PAYOUT".equals(templateType)) {
            return "ctrl_payout";
        } else if ("CONTRACT".equals(templateType)) {
            return "pact_main";
        } else if ("ASSET".equals(templateType)) {
            return "asset_purchase_apply_main";
        } else if ("HR_APPLY".equals(templateType) || templateType != null && templateType.startsWith("HR")) {
            return "hr_apply";
        }
        return "default";
    }
    
    /**
     * 从模板设置表获取业务类型
     * @param businessKey 业务主键
     * @return 业务类型（从template_config表的business_type字段获取），如果找不到返回null
     */
    private String getBusinessTypeFromTemplateConfig(String businessKey) {
        if (businessKey == null || businessKey.trim().isEmpty()) {
            return null;
        }
        
        try {
            Long templateConfigId = null;
            
            // 尝试从预算申请表获取templateConfigId
            if (budgServiceClient != null) {
                com.hrp.common.entity.Result<com.hrp.common.entity.BudgetApply> result = budgServiceClient.getBudgetApplyByNo(businessKey);
                if (result != null && result.getCode() == 200 && result.getData() != null) {
                    templateConfigId = result.getData().getTemplateConfigId();
                }
            }
            
            // 如果预算申请表没有找到，尝试从报账申请表获取templateConfigId
            if (templateConfigId == null && reimbServiceClient != null) {
                com.hrp.common.entity.Result<com.hrp.common.entity.CtrlPayout> result = reimbServiceClient.getCtrlPayoutByBillcode(businessKey);
                if (result != null && result.getCode() == 200 && result.getData() != null) {
                    templateConfigId = result.getData().getTemplateConfigId();
                }
            }

            if (templateConfigId == null && assetServiceClient != null) {
                com.hrp.common.entity.Result<com.hrp.common.entity.AssetPurchaseApplyMain> result = assetServiceClient.getAssetPurchaseApplyByNo(businessKey);
                if (result != null && result.getCode() == 200 && result.getData() != null) {
                    templateConfigId = result.getData().getTemplateConfigId();
                }
            }
            
            // 如果资产采购申请没有找到，尝试从HR业务申请表获取templateConfigId
            if (templateConfigId == null && hrServiceClient != null) {
                com.hrp.common.entity.Result<com.hrp.common.entity.HrApply> result = hrServiceClient.getHrApplyByApplyNo(businessKey);
                if (result != null && result.getCode() == 200 && result.getData() != null) {
                    templateConfigId = result.getData().getTemplateConfigId();
                }
            }
            
            // 通过templateConfigId查询模板设置表，获取business_type
            if (templateConfigId != null && templateConfigService != null) {
                com.hrp.common.entity.TemplateConfig templateConfig = templateConfigService.getById(templateConfigId);
                if (templateConfig != null && templateConfig.getBusinessType() != null) {
                    return templateConfig.getBusinessType(); // 从template_config表的business_type字段获取
                }
            }
        } catch (Exception e) {
            System.err.println("从模板设置表获取业务类型失败: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * 根据业务主键推断模板类型（备用方法）
     * 生成打印内容时，完全根据businessKey推断业务类型，不依赖templateType
     */
    private String inferTemplateTypeFromBusinessKey(String businessKey, String templateType) {
        if (businessKey == null || businessKey.trim().isEmpty()) {
            return templateType != null ? templateType : "CUSTOM";
        }
        
        String upperKey = businessKey.toUpperCase();
        
        // 根据业务主键前缀推断类型
        if (upperKey.startsWith("BUDG")) {
            // 预算申请
            return "BUDG";
        } else if (upperKey.startsWith("SQD")) {
            // 申请单
            return "APPLY";
        } else if (upperKey.startsWith("BXD")) {
            // 报销单
            return "PAYOUT";
        } else if (upperKey.startsWith("CONTRACT") || upperKey.startsWith("PACT")) {
            // 合同
            return "CONTRACT";
        } else if (upperKey.startsWith("ASSET")) {
            // 资产审批
            return "ASSET";
        } else if (upperKey.startsWith("HR")) {
            // HR业务申请
            return "HR";
        }
        
        // 如果无法推断，使用原始类型或默认值
        return templateType != null ? templateType : "CUSTOM";
    }
    
    /**
     * 将JSON格式的模板转换为HTML
     */
    private String convertJsonTemplateToHtml(String jsonContent, Map<String, Object> sourceData, PrintTemplate template) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonContent);
            JsonNode elementsNode = rootNode.get("elements");
            JsonNode pageDesignNode = rootNode.get("pageDesign");
            
            if (elementsNode == null || !elementsNode.isArray()) {
                throw new RuntimeException("模板JSON格式错误：缺少elements数组");
            }
            
            // 获取页面设置
            String pageSize = "A4";
            String orientation = "portrait";
            int columnCount = 4;
            int marginTop = 20, marginBottom = 20, marginLeft = 20, marginRight = 20;
            
            if (pageDesignNode != null) {
                if (pageDesignNode.has("pageSize")) {
                    pageSize = pageDesignNode.get("pageSize").asText();
                }
                if (pageDesignNode.has("orientation")) {
                    orientation = pageDesignNode.get("orientation").asText();
                }
                if (pageDesignNode.has("columnCount")) {
                    columnCount = pageDesignNode.get("columnCount").asInt();
                }
                if (pageDesignNode.has("marginTop")) {
                    marginTop = pageDesignNode.get("marginTop").asInt();
                }
                if (pageDesignNode.has("marginBottom")) {
                    marginBottom = pageDesignNode.get("marginBottom").asInt();
                }
                if (pageDesignNode.has("marginLeft")) {
                    marginLeft = pageDesignNode.get("marginLeft").asInt();
                }
                if (pageDesignNode.has("marginRight")) {
                    marginRight = pageDesignNode.get("marginRight").asInt();
                }
            }
            
            // 计算页面宽度（A4纵向：210mm，横向：297mm）
            double pageWidth = "landscape".equals(orientation) ? 297.0 : 210.0;
            double columnWidth = (pageWidth - marginLeft - marginRight) / columnCount;
            
            // 构建HTML
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html>\n");
            html.append("<html>\n<head>\n");
            html.append("<meta charset=\"UTF-8\">\n");
            html.append("<style>\n");
            html.append("body { margin: 0; padding: ").append(marginTop).append("mm ").append(marginRight).append("mm ").append(marginBottom).append("mm ").append(marginLeft).append("mm; font-family: Arial, sans-serif; }\n");
            html.append(".template-container { position: relative; width: 100%; }\n");
            html.append(".element { position: absolute; }\n");
            html.append(".title { font-weight: bold; text-align: center; }\n");
            html.append(".label-field { display: flex; align-items: center; }\n");
            html.append(".label { margin-right: 5px; }\n");
            html.append(".field { flex: 1; }\n");
            html.append("table { border-collapse: collapse; width: 100%; margin-top: 10px; }\n");
            html.append("table th, table td { border: 1px solid #000; padding: 5px; text-align: left; }\n");
            html.append("table th { background-color: #f0f0f0; font-weight: bold; }\n");
            html.append("</style>\n");
            html.append("</head>\n<body>\n");
            html.append("<div class=\"template-container\">\n");
            
            // 处理每个元素
            for (JsonNode elementNode : elementsNode) {
                String elementType = elementNode.has("type") ? elementNode.get("type").asText() : "";
                int x = elementNode.has("x") ? elementNode.get("x").asInt() : 0;
                int y = elementNode.has("y") ? elementNode.get("y").asInt() : 0;
                int width = elementNode.has("width") ? elementNode.get("width").asInt() : 100;
                int fontSize = elementNode.has("fontSize") ? elementNode.get("fontSize").asInt() : 12;
                String fontWeight = elementNode.has("fontWeight") ? elementNode.get("fontWeight").asText() : "normal";
                String textAlign = elementNode.has("textAlign") ? elementNode.get("textAlign").asText() : "left";
                
                // 计算实际位置（mm转px，1mm ≈ 3.779px）
                double left = (x * columnWidth / 100.0) * 3.779;
                double top = y * 3.779;
                double elementWidth = (width * columnWidth / 100.0) * 3.779;
                
                html.append("<div class=\"element\" style=\"left: ").append(left).append("px; top: ").append(top).append("px; width: ").append(elementWidth).append("px; font-size: ").append(fontSize).append("px; font-weight: ").append(fontWeight).append("; text-align: ").append(textAlign).append(";\">\n");
                
                if ("title".equals(elementType)) {
                    // 标题
                    String content = elementNode.has("content") ? elementNode.get("content").asText() : "";
                    html.append("<div class=\"title\">").append(escapeHtml(content)).append("</div>\n");
                } else if ("label-field".equals(elementType)) {
                    // 标签字段
                    String labelText = elementNode.has("labelText") ? elementNode.get("labelText").asText() : "";
                    String fieldKey = elementNode.has("fieldKey") ? elementNode.get("fieldKey").asText() : "";
                    String value = getFieldValue(fieldKey, sourceData);
                    html.append("<div class=\"label-field\">");
                    html.append("<span class=\"label\">").append(escapeHtml(labelText)).append("</span>");
                    html.append("<span class=\"field\">").append(escapeHtml(value)).append("</span>");
                    html.append("</div>\n");
                } else if ("table".equals(elementType)) {
                    // 表格
                    JsonNode columnsNode = elementNode.get("columns");
                    if (columnsNode != null && columnsNode.isArray()) {
                        html.append("<table>\n<thead>\n<tr>\n");
                        // 表头
                        for (JsonNode columnNode : columnsNode) {
                            String label = columnNode.has("label") ? columnNode.get("label").asText() : "";
                            html.append("<th>").append(escapeHtml(label)).append("</th>\n");
                        }
                        html.append("</tr>\n</thead>\n<tbody>\n");
                        
                        // 表格数据行
                        // 检查是否是流程任务表（wf_process_task），如果是，需要处理多行数据（有特殊逻辑）
                        boolean isProcessTaskTable = false;
                        boolean isIndexedTable = false;
                        String tablePrefix = null;
                        
                        for (JsonNode columnNode : columnsNode) {
                            String fieldKey = columnNode.has("fieldKey") ? columnNode.get("fieldKey").asText() : "";
                            if (fieldKey.startsWith("wf_process_task.") || fieldKey.startsWith("process_task.")) {
                                isProcessTaskTable = true;
                                break;
                            }
                            // 检查是否是带索引的表格（如 detail[0].asset_code, budget_detail[0].item_code）
                            if (fieldKey.contains("[") && fieldKey.contains("].")) {
                                isIndexedTable = true;
                                // 提取表前缀（如 detail[0].asset_code -> detail）
                                int bracketStart = fieldKey.indexOf("[");
                                if (bracketStart > 0) {
                                    tablePrefix = fieldKey.substring(0, bracketStart);
                                }
                                break;
                            } else if (fieldKey.contains(".") && !fieldKey.startsWith("wf_process_task.") && !fieldKey.startsWith("process_task.")) {
                                // 也可能是 detail.asset_code 格式（不带索引，但可能有多行数据）
                                String prefix = fieldKey.substring(0, fieldKey.indexOf("."));
                                // 检查是否是已知的明细表前缀
                                if (prefix.equals("detail") || prefix.equals("budget_detail") || 
                                    prefix.equals("ctrl_payout_payment") || prefix.equals("ctrl_payout_invoice") ||
                                    prefix.equals("asset_purchase_apply_detail")) {
                                    isIndexedTable = true;
                                    tablePrefix = prefix;
                                    break;
                                }
                            }
                        }
                        
                        if (isProcessTaskTable) {
                            // 流程任务表，需要处理多行数据（有特殊逻辑：只显示comment有值的，需要排序）
                            List<Map<String, String>> taskRows = getProcessTaskTableData(columnsNode, sourceData);
                            for (Map<String, String> row : taskRows) {
                                html.append("<tr>\n");
                                for (JsonNode columnNode : columnsNode) {
                                    String fieldKey = columnNode.has("fieldKey") ? columnNode.get("fieldKey").asText() : "";
                                    String value = row.getOrDefault(fieldKey, "");
                                    // 如果是签名字段且是HTML格式，不需要转义
                                    if ((fieldKey.contains("approver_signature") || fieldKey.contains("approverSignature")) 
                                        && value.trim().startsWith("<img")) {
                                        html.append("<td>").append(value).append("</td>\n");
                                    } else {
                                        html.append("<td>").append(escapeHtml(value)).append("</td>\n");
                                    }
                                }
                                html.append("</tr>\n");
                            }
                        } else if (isIndexedTable && tablePrefix != null) {
                            // 带索引的明细表，需要处理多行数据（通用方法）
                            List<Map<String, String>> detailRows = getIndexedTableData(columnsNode, sourceData, tablePrefix);
                            for (Map<String, String> row : detailRows) {
                                html.append("<tr>\n");
                                for (JsonNode columnNode : columnsNode) {
                                    String fieldKey = columnNode.has("fieldKey") ? columnNode.get("fieldKey").asText() : "";
                                    String value = row.getOrDefault(fieldKey, "");
                                    html.append("<td>").append(escapeHtml(value)).append("</td>\n");
                                }
                                html.append("</tr>\n");
                            }
                        } else {
                            // 普通表，只有一行数据
                            html.append("<tr>\n");
                            for (JsonNode columnNode : columnsNode) {
                                String fieldKey = columnNode.has("fieldKey") ? columnNode.get("fieldKey").asText() : "";
                                String value = getFieldValue(fieldKey, sourceData);
                                html.append("<td>").append(escapeHtml(value)).append("</td>\n");
                            }
                            html.append("</tr>\n");
                        }
                        
                        html.append("</tbody>\n</table>\n");
                    }
                } else if ("text".equals(elementType)) {
                    // 文本
                    String content = elementNode.has("content") ? elementNode.get("content").asText() : "";
                    html.append("<div>").append(escapeHtml(content)).append("</div>\n");
                }
                
                html.append("</div>\n");
            }
            
            html.append("</div>\n");
            html.append("</body>\n</html>");
            
            return html.toString();
        } catch (Exception e) {
            System.err.println("转换JSON模板为HTML失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("转换JSON模板为HTML失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取字段值
     */
    private String getFieldValue(String fieldKey, Map<String, Object> sourceData) {
        if (fieldKey == null || fieldKey.isEmpty()) {
            return "";
        }
        
        // 直接查找
        if (sourceData.containsKey(fieldKey)) {
            Object value = sourceData.get(fieldKey);
            return value != null ? value.toString() : "";
        }
        
        // 尝试不同的格式
        String[] variations = {
            fieldKey,
            fieldKey.toLowerCase(),
            fieldKey.replace(".", "_"),
            fieldKey.replace("_", ".")
        };
        
        for (String key : variations) {
            if (sourceData.containsKey(key)) {
                Object value = sourceData.get(key);
                return value != null ? value.toString() : "";
            }
        }
        
        return "";
    }
    
    /**
     * 获取流程任务表格数据（多行）
     * 只返回comment有值的记录，并按printOrder排序
     */
    private List<Map<String, String>> getProcessTaskTableData(JsonNode columnsNode, Map<String, Object> sourceData) {
        List<Map<String, String>> rows = new ArrayList<>();
        
        // 查找所有流程任务数据（wf_process_task[0], wf_process_task[1], ...）
        // 同时收集每个任务的printOrder用于排序
        Map<Integer, Map<String, String>> taskRows = new HashMap<>();
        Map<Integer, Integer> printOrders = new HashMap<>();
        int maxIndex = 0;
        
        for (String key : sourceData.keySet()) {
            if (key.startsWith("wf_process_task[") && key.contains("].")) {
                int start = key.indexOf("[") + 1;
                int end = key.indexOf("]");
                if (start > 0 && end > start) {
                    try {
                        int index = Integer.parseInt(key.substring(start, end));
                        if (index > maxIndex) {
                            maxIndex = index;
                        }
                    } catch (NumberFormatException e) {
                        // 忽略
                    }
                }
            }
        }
        
        // 为每个任务创建一行数据，并检查comment是否有值
        for (int i = 0; i <= maxIndex; i++) {
            // 检查comment字段是否有值
            String commentKey = "wf_process_task[" + i + "].comment";
            Object commentObj = sourceData.get(commentKey);
            String comment = commentObj != null ? commentObj.toString() : "";
            
            // 只处理comment有值的记录
            if (comment != null && !comment.trim().isEmpty()) {
                Map<String, String> row = new HashMap<>();
                Integer printOrder = null;
                
                for (JsonNode columnNode : columnsNode) {
                    String fieldKey = columnNode.has("fieldKey") ? columnNode.get("fieldKey").asText() : "";
                    // 将 wf_process_task.fieldName 转换为 wf_process_task[i].fieldName
                    String indexedKey = fieldKey.replace("wf_process_task.", "wf_process_task[" + i + "].");
                    indexedKey = indexedKey.replace("process_task.", "wf_process_task[" + i + "].");
                    
                    String value = "";
                    if (sourceData.containsKey(indexedKey)) {
                        Object obj = sourceData.get(indexedKey);
                        value = obj != null ? obj.toString() : "";
                    } else if (sourceData.containsKey(fieldKey)) {
                        // 如果没有索引版本，使用原始key（可能是第一个任务的数据）
                        Object obj = sourceData.get(fieldKey);
                        value = obj != null ? obj.toString() : "";
                    }
                    
                    // 如果是签名字段，确保是HTML格式（如果已经是HTML则不变，如果是base64则转换）
                    if (fieldKey.contains("approver_signature") || fieldKey.contains("approverSignature")) {
                        if (value != null && !value.trim().isEmpty() && !value.trim().startsWith("<img")) {
                            // 如果值是base64字符串，转换为img标签
                            String signatureBase64 = value.trim();
                            if (!signatureBase64.startsWith("data:image")) {
                                signatureBase64 = "data:image/png;base64," + signatureBase64;
                            }
                            value = "<img src=\"" + signatureBase64 + "\" style=\"max-width: 100px; max-height: 50px;\" />";
                        }
                    }
                    
                    row.put(fieldKey, value);
                }
                
                // 从sourceData中获取printOrder用于排序
                Object printOrderObj = sourceData.get("wf_process_task[" + i + "].print_order");
                if (printOrderObj != null) {
                    try {
                        printOrder = Integer.parseInt(printOrderObj.toString());
                    } catch (NumberFormatException e) {
                        // 忽略
                    }
                }
                
                taskRows.put(i, row);
                printOrders.put(i, printOrder != null ? printOrder : 999999); // null排在最后
            }
        }
        
        // 按printOrder排序
        List<Integer> sortedIndexes = new ArrayList<>(taskRows.keySet());
        sortedIndexes.sort((i1, i2) -> {
            Integer order1 = printOrders.get(i1);
            Integer order2 = printOrders.get(i2);
            return order1.compareTo(order2);
        });
        
        // 按排序后的顺序添加到结果列表
        for (Integer index : sortedIndexes) {
            rows.add(taskRows.get(index));
        }
        
        return rows;
    }
    
    /**
     * 获取带索引的表格数据（通用方法，处理所有明细表的多行数据）
     * 支持的格式：
     * - detail[0].asset_code, detail[1].asset_code （资产明细）
     * - budget_detail[0].item_code, budget_detail[1].item_code （预算明细）
     * - ctrl_payout_payment[0].payment_amount, ctrl_payout_payment[1].payment_amount （支付明细）
     * - ctrl_payout_invoice[0].invoice_code, ctrl_payout_invoice[1].invoice_code （发票信息）
     * - asset_purchase_apply_detail[0].asset_code （资产采购明细）
     * 
     * @param columnsNode 表格列的JSON节点
     * @param sourceData 数据源
     * @param tablePrefix 表前缀（如 detail, budget_detail, ctrl_payout_payment 等）
     * @return 多行数据列表
     */
    private List<Map<String, String>> getIndexedTableData(JsonNode columnsNode, Map<String, Object> sourceData, String tablePrefix) {
        List<Map<String, String>> rows = new ArrayList<>();
        
        // 查找所有带索引的数据，确定最大索引
        int maxIndex = -1;
        for (String key : sourceData.keySet()) {
            // 匹配格式：表前缀[索引].字段名 或 表前缀.字段名（向后兼容）
            if (key.startsWith(tablePrefix + "[") && key.contains("].")) {
                int start = tablePrefix.length() + 1; // 跳过 "表前缀["
                int end = key.indexOf("]", start);
                if (end > start) {
                    try {
                        int index = Integer.parseInt(key.substring(start, end));
                        if (index > maxIndex) {
                            maxIndex = index;
                        }
                    } catch (NumberFormatException e) {
                        // 忽略
                    }
                }
            }
        }
        
        // 如果没有找到带索引的数据，尝试从列表数据中获取
        if (maxIndex < 0) {
            // 尝试从列表数据中获取（如 asset_purchase_apply_detail_list, detailList）
            Object listData = sourceData.get(tablePrefix + "_list");
            if (listData == null) {
                // 尝试其他可能的列表键名
                if (tablePrefix.equals("detail") || tablePrefix.equals("asset_purchase_apply_detail")) {
                    listData = sourceData.get("detailList");
                    if (listData == null) {
                        listData = sourceData.get("asset_purchase_apply_detail_list");
                    }
                }
            }
            
            if (listData instanceof List) {
                List<?> list = (List<?>) listData;
                maxIndex = list.size() - 1;
            }
        }
        
        // 如果没有数据，返回空列表
        if (maxIndex < 0) {
            return rows;
        }
        
        // 为每个索引创建一行数据
        for (int i = 0; i <= maxIndex; i++) {
            Map<String, String> row = new HashMap<>();
            boolean hasData = false;
            
            for (JsonNode columnNode : columnsNode) {
                String fieldKey = columnNode.has("fieldKey") ? columnNode.get("fieldKey").asText() : "";
                if (fieldKey == null || fieldKey.isEmpty()) {
                    continue;
                }
                
                String value = "";
                
                // 尝试多种格式的key
                String[] keyVariations = {
                    // 带索引格式：detail[0].asset_code
                    tablePrefix + "[" + i + "]." + extractFieldName(fieldKey, tablePrefix),
                    // 带索引格式（完整fieldKey）：如果fieldKey已经是 detail[0].asset_code，直接使用
                    fieldKey.contains("[") ? fieldKey.replaceFirst("\\[\\d+\\]", "[" + i + "]") : null,
                    // 不带索引格式（向后兼容）：detail.asset_code（仅第一条）
                    i == 0 ? (tablePrefix + "." + extractFieldName(fieldKey, tablePrefix)) : null,
                    // 原始fieldKey（如果已经是正确格式）
                    fieldKey.startsWith(tablePrefix) ? fieldKey : null
                };
                
                // 尝试从sourceData中获取值
                for (String keyVar : keyVariations) {
                    if (keyVar != null && sourceData.containsKey(keyVar)) {
                        Object obj = sourceData.get(keyVar);
                        value = obj != null ? obj.toString() : "";
                        hasData = true;
                        break;
                    }
                }
                
                // 如果仍然没有找到，尝试从列表数据中获取
                if (value.isEmpty() && sourceData.containsKey(tablePrefix + "_list")) {
                    Object listData = sourceData.get(tablePrefix + "_list");
                    if (listData instanceof List && i < ((List<?>) listData).size()) {
                        Object item = ((List<?>) listData).get(i);
                        if (item instanceof Map) {
                            Map<?, ?> itemMap = (Map<?, ?>) item;
                            String fieldName = extractFieldName(fieldKey, tablePrefix);
                            // 尝试多种字段名格式
                            Object fieldValue = itemMap.get(fieldName);
                            if (fieldValue == null) {
                                // 尝试驼峰格式
                                fieldValue = itemMap.get(convertSnakeToCamel(fieldName));
                            }
                            if (fieldValue == null) {
                                // 尝试原始fieldKey中的字段名
                                fieldValue = itemMap.get(fieldKey.substring(fieldKey.lastIndexOf(".") + 1));
                            }
                            if (fieldValue != null) {
                                value = fieldValue.toString();
                                hasData = true;
                            }
                        }
                    }
                }
                
                row.put(fieldKey, value);
            }
            
            // 只有当至少有一个字段有数据时才添加这一行
            if (hasData) {
                rows.add(row);
            }
        }
        
        return rows;
    }
    
    /**
     * 从fieldKey中提取字段名
     * 例如：detail[0].asset_code -> asset_code, detail.asset_code -> asset_code
     */
    private String extractFieldName(String fieldKey, String tablePrefix) {
        if (fieldKey == null || fieldKey.isEmpty()) {
            return "";
        }
        
        // 如果fieldKey包含表前缀，提取后面的部分
        if (fieldKey.startsWith(tablePrefix)) {
            String remainder = fieldKey.substring(tablePrefix.length());
            // 跳过 [索引] 或 .
            if (remainder.startsWith("[")) {
                int bracketEnd = remainder.indexOf("]");
                if (bracketEnd > 0 && bracketEnd < remainder.length() - 1) {
                    return remainder.substring(bracketEnd + 2); // 跳过 "]."
                }
            } else if (remainder.startsWith(".")) {
                return remainder.substring(1); // 跳过 "."
            }
        }
        
        // 如果不包含表前缀，直接返回最后一个点后面的部分
        int lastDot = fieldKey.lastIndexOf(".");
        if (lastDot >= 0 && lastDot < fieldKey.length() - 1) {
            return fieldKey.substring(lastDot + 1);
        }
        
        return fieldKey;
    }
    
    /**
     * 将下划线命名转换为驼峰命名
     * 例如：asset_code -> assetCode
     */
    private String convertSnakeToCamel(String snakeCase) {
        if (snakeCase == null || snakeCase.isEmpty()) {
            return snakeCase;
        }
        
        StringBuilder result = new StringBuilder();
        boolean nextUpperCase = false;
        for (char c : snakeCase.toCharArray()) {
            if (c == '_') {
                nextUpperCase = true;
            } else {
                if (nextUpperCase) {
                    result.append(Character.toUpperCase(c));
                    nextUpperCase = false;
                } else {
                    result.append(c);
                }
            }
        }
        return result.toString();
    }
    
    /**
     * HTML转义
     */
    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
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

            // 获取模板内容（只使用templateJson）
            String templateJson = template.getTemplateJson();

            if (templateJson == null || templateJson.isEmpty()) {
                throw new RuntimeException("模板内容为空，无法生成预览");
            }

            // 使用JSON格式模板生成预览
            String html = convertJsonTemplateToHtml(templateJson, sourceData, template);

            return html;
        } catch (Exception e) {
            throw new RuntimeException("预览失败：" + e.getMessage(), e);
        }
    }

    @Override
    public String exportTemplate(PrintTemplate template) {
        try {
            // 构建导出JSON对象，包含所有字段（即使值为null也包含）
            Map<String, Object> exportData = new HashMap<>();
            exportData.put("templateCode", template.getTemplateCode());
            exportData.put("templateName", template.getTemplateName());
            exportData.put("templateType", template.getTemplateType());
            exportData.put("templateContent", template.getTemplateContent());
            exportData.put("templateXml", template.getTemplateXml());
            exportData.put("templateJson", template.getTemplateJson());
            exportData.put("templateFields", template.getTemplateFields()); // 打印的数据全部在这里
            exportData.put("pageSize", template.getPageSize());
            exportData.put("orientation", template.getOrientation());
            exportData.put("marginTop", template.getMarginTop());
            exportData.put("marginBottom", template.getMarginBottom());
            exportData.put("marginLeft", template.getMarginLeft());
            exportData.put("marginRight", template.getMarginRight());
            exportData.put("customCss", template.getCustomCss());
            exportData.put("headerHtml", template.getHeaderHtml());
            exportData.put("footerHtml", template.getFooterHtml());
            exportData.put("isDefault", template.getIsDefault());
            exportData.put("isActive", template.getIsActive());
            exportData.put("remark", template.getRemark());
            exportData.put("createUser", template.getCreateUser());
            exportData.put("exportTime", java.time.LocalDateTime.now().toString());
            exportData.put("exportVersion", "1.0");
            
            // 转换为JSON字符串，包含null值
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(exportData);
        } catch (Exception e) {
            throw new RuntimeException("导出模板失败：" + e.getMessage(), e);
        }
    }

    @Override
    public PrintTemplate importTemplate(String jsonContent, String templateCode, String templateName, String templateType, String createUser) {
        try {
            // 解析JSON内容
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonContent);
            
            // 创建新模板对象
            PrintTemplate template = new PrintTemplate();
            
            // 从JSON中读取或使用传入的参数（优先使用传入的参数）
            template.setTemplateCode(templateCode != null && !templateCode.isEmpty() ? templateCode : 
                (jsonNode.has("templateCode") && !jsonNode.get("templateCode").isNull() ? jsonNode.get("templateCode").asText() : null));
            template.setTemplateName(templateName != null && !templateName.isEmpty() ? templateName : 
                (jsonNode.has("templateName") && !jsonNode.get("templateName").isNull() ? jsonNode.get("templateName").asText() : null));
            template.setTemplateType(templateType != null && !templateType.isEmpty() ? templateType : 
                (jsonNode.has("templateType") && !jsonNode.get("templateType").isNull() ? jsonNode.get("templateType").asText() : "CUSTOM"));
            
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
            
            // 从JSON中读取模板内容（即使为null也设置）
            if (jsonNode.has("templateContent")) {
                JsonNode templateContentNode = jsonNode.get("templateContent");
                if (templateContentNode.isNull()) {
                    template.setTemplateContent(null);
                } else {
                    template.setTemplateContent(templateContentNode.asText());
                }
            }
            if (jsonNode.has("templateXml")) {
                JsonNode templateXmlNode = jsonNode.get("templateXml");
                if (templateXmlNode.isNull()) {
                    template.setTemplateXml(null);
                } else {
                    template.setTemplateXml(templateXmlNode.asText());
                }
            }
            if (jsonNode.has("templateJson")) {
                JsonNode templateJsonNode = jsonNode.get("templateJson");
                if (templateJsonNode.isNull()) {
                    template.setTemplateJson(null);
                } else {
                    template.setTemplateJson(templateJsonNode.asText());
                }
            }
            if (jsonNode.has("templateFields")) {
                JsonNode templateFieldsNode = jsonNode.get("templateFields");
                if (templateFieldsNode.isNull()) {
                    template.setTemplateFields(null);
                } else {
                    template.setTemplateFields(templateFieldsNode.asText());
                }
            }
            if (jsonNode.has("pageSize")) {
                JsonNode pageSizeNode = jsonNode.get("pageSize");
                if (pageSizeNode.isNull()) {
                    template.setPageSize("A4");
                } else {
                    template.setPageSize(pageSizeNode.asText());
                }
            } else {
                template.setPageSize("A4");
            }
            if (jsonNode.has("orientation")) {
                JsonNode orientationNode = jsonNode.get("orientation");
                if (orientationNode.isNull()) {
                    template.setOrientation("portrait");
                } else {
                    template.setOrientation(orientationNode.asText());
                }
            } else {
                template.setOrientation("portrait");
            }
            if (jsonNode.has("marginTop")) {
                JsonNode marginTopNode = jsonNode.get("marginTop");
                if (marginTopNode.isNull()) {
                    template.setMarginTop(20);
                } else {
                    template.setMarginTop(marginTopNode.asInt());
                }
            } else {
                template.setMarginTop(20);
            }
            if (jsonNode.has("marginBottom")) {
                JsonNode marginBottomNode = jsonNode.get("marginBottom");
                if (marginBottomNode.isNull()) {
                    template.setMarginBottom(20);
                } else {
                    template.setMarginBottom(marginBottomNode.asInt());
                }
            } else {
                template.setMarginBottom(20);
            }
            if (jsonNode.has("marginLeft")) {
                JsonNode marginLeftNode = jsonNode.get("marginLeft");
                if (marginLeftNode.isNull()) {
                    template.setMarginLeft(20);
                } else {
                    template.setMarginLeft(marginLeftNode.asInt());
                }
            } else {
                template.setMarginLeft(20);
            }
            if (jsonNode.has("marginRight")) {
                JsonNode marginRightNode = jsonNode.get("marginRight");
                if (marginRightNode.isNull()) {
                    template.setMarginRight(20);
                } else {
                    template.setMarginRight(marginRightNode.asInt());
                }
            } else {
                template.setMarginRight(20);
            }
            if (jsonNode.has("customCss")) {
                JsonNode customCssNode = jsonNode.get("customCss");
                if (customCssNode.isNull()) {
                    template.setCustomCss(null);
                } else {
                    template.setCustomCss(customCssNode.asText());
                }
            }
            if (jsonNode.has("headerHtml")) {
                JsonNode headerHtmlNode = jsonNode.get("headerHtml");
                if (headerHtmlNode.isNull()) {
                    template.setHeaderHtml(null);
                } else {
                    template.setHeaderHtml(headerHtmlNode.asText());
                }
            }
            if (jsonNode.has("footerHtml")) {
                JsonNode footerHtmlNode = jsonNode.get("footerHtml");
                if (footerHtmlNode.isNull()) {
                    template.setFooterHtml(null);
                } else {
                    template.setFooterHtml(footerHtmlNode.asText());
                }
            }
            if (jsonNode.has("isDefault")) {
                JsonNode isDefaultNode = jsonNode.get("isDefault");
                if (isDefaultNode.isNull()) {
                    template.setIsDefault(0);
                } else if (isDefaultNode.isNumber()) {
                    template.setIsDefault(isDefaultNode.asInt());
                } else if (isDefaultNode.isBoolean()) {
                    template.setIsDefault(isDefaultNode.asBoolean() ? 1 : 0);
                } else {
                    template.setIsDefault(0);
                }
            } else {
                template.setIsDefault(0);
            }
            if (jsonNode.has("isActive")) {
                JsonNode isActiveNode = jsonNode.get("isActive");
                if (isActiveNode.isNull()) {
                    template.setIsActive(1);
                } else if (isActiveNode.isNumber()) {
                    template.setIsActive(isActiveNode.asInt());
                } else if (isActiveNode.isBoolean()) {
                    template.setIsActive(isActiveNode.asBoolean() ? 1 : 0);
                } else {
                    template.setIsActive(1);
                }
            } else {
                template.setIsActive(1);
            }
            if (jsonNode.has("remark")) {
                JsonNode remarkNode = jsonNode.get("remark");
                if (remarkNode.isNull()) {
                    template.setRemark(null);
                } else {
                    String remarkValue = remarkNode.asText();
                    if (remarkValue != null && !"null".equals(remarkValue)) {
                        template.setRemark(remarkValue);
                    } else {
                        template.setRemark(null);
                    }
                }
            }
            
            // 设置创建用户（导入操作者）
            if (createUser != null && !createUser.isEmpty()) {
                template.setCreateUser(createUser);
            } else if (jsonNode.has("createUser")) {
                JsonNode createUserNode = jsonNode.get("createUser");
                if (!createUserNode.isNull()) {
                    String createUserValue = createUserNode.asText();
                    if (createUserValue != null && !createUserValue.isEmpty() && !"null".equals(createUserValue)) {
                        template.setCreateUser(createUserValue);
                    }
                }
            }
            
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

