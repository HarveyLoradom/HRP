package com.hrp.auth.service.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrp.auth.feign.AssetServiceClient;
import com.hrp.auth.feign.BudgServiceClient;
import com.hrp.auth.feign.ContractServiceClient;
import com.hrp.auth.feign.HrServiceClient;
import com.hrp.auth.mapper.ProcessDefinitionMapper;
import com.hrp.auth.service.DeptService;
import com.hrp.auth.service.PositionService;
import com.hrp.auth.service.ProcessDefinitionService;
import com.hrp.auth.service.UserEmployeeService;
import com.hrp.common.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.*;

/**
 * 流程定义服务实现类
 */
@Service
public class ProcessDefinitionServiceImpl implements ProcessDefinitionService {

    @Autowired
    private ProcessDefinitionMapper processDefinitionMapper;
    
    @Autowired(required = false)
    private BudgServiceClient budgServiceClient;
    
    @Autowired(required = false)
    private com.hrp.auth.feign.ReimbServiceClient reimbServiceClient;
    
    @Autowired(required = false)
    private ContractServiceClient contractServiceClient;
    
    @Autowired(required = false)
    private AssetServiceClient assetServiceClient;
    
    @Autowired(required = false)
    private HrServiceClient hrServiceClient;
    
    @Autowired(required = false)
    private DeptService deptService;
    
    @Autowired(required = false)
    private UserEmployeeService userEmployeeService;
    
    @Autowired(required = false)
    private com.hrp.auth.service.UserService userService;
    
    @Autowired(required = false)
    private PositionService positionService;
    
    @Autowired(required = false)
    private com.hrp.auth.service.TemplateConfigService templateConfigService;

    @Override
    public ProcessDefinition getById(Long definitionId) {
        return processDefinitionMapper.selectById(definitionId);
    }

    @Override
    public ProcessDefinition getByKey(String definitionKey) {
        return processDefinitionMapper.selectByKey(definitionKey);
    }

    @Override
    public List<ProcessDefinition> getByType(String definitionType, Long isActive) {
        return processDefinitionMapper.selectByType(definitionType, isActive);
    }

    @Override
    public List<ProcessDefinition> getAll(Long isActive) {
        return processDefinitionMapper.selectAll(isActive);
    }

    @Override
    public PageResult<ProcessDefinition> getAllPage(Long isActive, Long page, Long size) {
        if (page == null || page < 1) {
            page = 1L;
        }
        if (size == null || size < 1) {
            size = 10L;
        }
        Long offset = (page - 1) * size;
        List<ProcessDefinition> list = processDefinitionMapper.selectAllPage(isActive, offset, size);
        Long total = processDefinitionMapper.countAll(isActive);
        return new PageResult<>(list, total, size, page);
    }

    @Override
    public PageResult<ProcessDefinition> getByTypePage(String definitionType, Long isActive, Long page, Long size) {
        if (page == null || page < 1) {
            page = 1L;
        }
        if (size == null || size < 1) {
            size = 10L;
        }
        Long offset = (page - 1) * size;
        List<ProcessDefinition> list = processDefinitionMapper.selectByTypePage(definitionType, isActive, offset, size);
        Long total = processDefinitionMapper.countByType(definitionType, isActive);
        return new PageResult<>(list, total, size, page);
    }

    @Override
    @Transactional
    public boolean save(ProcessDefinition definition) {
        if (definition.getVersion() == null) {
            definition.setVersion(1);
        }
        if (definition.getIsActive() == null) {
            definition.setIsActive(1L);
        }
        return processDefinitionMapper.insert(definition) > 0;
    }

    @Override
    @Transactional
    public boolean update(ProcessDefinition definition) {
        // 如果更新了流程编码，需要检查唯一性（排除当前记录）
        if (definition.getDefinitionKey() != null && !definition.getDefinitionKey().isEmpty()) {
            ProcessDefinition existDefinition = processDefinitionMapper.selectByKey(definition.getDefinitionKey());
            if (existDefinition != null && !existDefinition.getDefinitionId().equals(definition.getDefinitionId())) {
                throw new RuntimeException("流程编码已存在：" + definition.getDefinitionKey());
            }
        }
        return processDefinitionMapper.updateById(definition) > 0;
    }

    @Override
    @Transactional
    public boolean delete(Long definitionId) {
        return processDefinitionMapper.deleteById(definitionId) > 0;
    }

    @Override
    public String exportDefinition(ProcessDefinition definition) {
        try {
            // 构建导出JSON对象，包含所有字段（即使值为null也包含）
            Map<String, Object> exportData = new HashMap<>();
            exportData.put("definitionKey", definition.getDefinitionKey());
            exportData.put("definitionName", definition.getDefinitionName());
            exportData.put("definitionType", definition.getDefinitionType());
            exportData.put("businessType", definition.getBusinessType());
            exportData.put("processXml", definition.getProcessXml());
            exportData.put("processJson", definition.getProcessJson());
            exportData.put("version", definition.getVersion());
            exportData.put("isActive", definition.getIsActive());
            exportData.put("description", definition.getDescription());
            exportData.put("createUser", definition.getCreateUser());
            exportData.put("exportTime", java.time.LocalDateTime.now().toString());
            exportData.put("exportVersion", "1.0");
            
            // 转换为JSON字符串，包含null值
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(exportData);
        } catch (Exception e) {
            throw new RuntimeException("导出流程定义失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public ProcessDefinition importDefinition(String jsonContent, String definitionKey, String definitionName, String definitionType, String createUser) {
        try {
            // 创建新流程定义对象
            ProcessDefinition definition = new ProcessDefinition();
            
            // 检测内容格式：如果是XML格式（以<?xml开头），则作为XML处理；否则作为JSON处理
            String trimmedContent = jsonContent != null ? jsonContent.trim() : "";
            boolean isXmlFormat = trimmedContent.startsWith("<?xml") || trimmedContent.startsWith("<bpmn2:") || trimmedContent.startsWith("<bpmn:");
            
            if (isXmlFormat) {
                // XML格式：直接保存为processXml
                definition.setProcessXml(jsonContent);
                definition.setProcessJson(null);
                
                // 使用传入的参数或生成默认值
                if (definitionKey != null && !definitionKey.isEmpty()) {
                    definition.setDefinitionKey(definitionKey);
                } else {
                    definition.setDefinitionKey("PROCESS_" + System.currentTimeMillis());
                }
                
                if (definitionName != null && !definitionName.isEmpty()) {
                    definition.setDefinitionName(definitionName);
                } else {
                    definition.setDefinitionName(definition.getDefinitionKey());
                }
                
                definition.setDefinitionType(definitionType != null ? definitionType : "CUSTOM");
                definition.setVersion(1);
                definition.setIsActive(1L);
            } else {
                // JSON格式：解析JSON内容
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(jsonContent);
                
                // 从JSON中读取或使用传入的参数（优先使用传入的参数）
                definition.setDefinitionKey(definitionKey != null && !definitionKey.isEmpty() ? definitionKey : 
                    (jsonNode.has("definitionKey") && !jsonNode.get("definitionKey").isNull() ? jsonNode.get("definitionKey").asText() : null));
                definition.setDefinitionName(definitionName != null && !definitionName.isEmpty() ? definitionName : 
                    (jsonNode.has("definitionName") && !jsonNode.get("definitionName").isNull() ? jsonNode.get("definitionName").asText() : null));
                definition.setDefinitionType(definitionType != null && !definitionType.isEmpty() ? definitionType : 
                    (jsonNode.has("definitionType") && !jsonNode.get("definitionType").isNull() ? jsonNode.get("definitionType").asText() : "CUSTOM"));
                
                // 从JSON中读取业务类型（即使为null也设置）
                if (jsonNode.has("businessType")) {
                    JsonNode businessTypeNode = jsonNode.get("businessType");
                    if (businessTypeNode.isNull()) {
                        definition.setBusinessType(null);
                    } else {
                        String businessTypeValue = businessTypeNode.asText();
                        if (businessTypeValue != null && !businessTypeValue.isEmpty() && !"null".equals(businessTypeValue)) {
                            definition.setBusinessType(businessTypeValue);
                        } else {
                            definition.setBusinessType(null);
                        }
                    }
                }
                
                // 从JSON中读取流程内容（即使为null也设置）
                if (jsonNode.has("processXml")) {
                    JsonNode processXmlNode = jsonNode.get("processXml");
                    if (processXmlNode.isNull()) {
                        definition.setProcessXml(null);
                    } else {
                        definition.setProcessXml(processXmlNode.asText());
                    }
                }
                if (jsonNode.has("processJson")) {
                    JsonNode processJsonNode = jsonNode.get("processJson");
                    if (processJsonNode.isNull()) {
                        definition.setProcessJson(null);
                    } else {
                        definition.setProcessJson(processJsonNode.asText());
                    }
                }
                if (jsonNode.has("version")) {
                    JsonNode versionNode = jsonNode.get("version");
                    if (versionNode.isNull()) {
                        definition.setVersion(1);
                    } else {
                        definition.setVersion(versionNode.asInt());
                    }
                } else {
                    definition.setVersion(1);
                }
                if (jsonNode.has("isActive")) {
                    JsonNode isActiveNode = jsonNode.get("isActive");
                    if (isActiveNode.isNull()) {
                        definition.setIsActive(1L);
                    } else if (isActiveNode.isNumber()) {
                        definition.setIsActive(isActiveNode.asLong());
                    } else if (isActiveNode.isBoolean()) {
                        definition.setIsActive(isActiveNode.asBoolean() ? 1L : 0L);
                    } else {
                        definition.setIsActive(1L);
                    }
                }
                if (jsonNode.has("description")) {
                    JsonNode descNode = jsonNode.get("description");
                    if (descNode.isNull()) {
                        definition.setDescription(null);
                    } else {
                        String descValue = descNode.asText();
                        if (descValue != null && !"null".equals(descValue)) {
                            definition.setDescription(descValue);
                        } else {
                            definition.setDescription(null);
                        }
                    }
                }
                // 从JSON中读取createUser（如果存在，但导入时会用操作者覆盖）
                if (jsonNode.has("createUser")) {
                    JsonNode createUserNode = jsonNode.get("createUser");
                    if (!createUserNode.isNull()) {
                        String createUserValue = createUserNode.asText();
                        // 如果导入时没有传入createUser，使用JSON中的值
                        if (createUser == null || createUser.isEmpty()) {
                            definition.setCreateUser(createUserValue);
                        }
                    }
                }
            }
            
            // 检查KEY是否已存在
            if (definition.getDefinitionKey() != null) {
                ProcessDefinition existDefinition = processDefinitionMapper.selectByKey(definition.getDefinitionKey());
                if (existDefinition != null) {
                    // 如果已存在，生成新的KEY
                    definition.setDefinitionKey(definition.getDefinitionKey() + "_" + System.currentTimeMillis());
                }
            } else {
                // 如果没有KEY，生成一个
                definition.setDefinitionKey("PROCESS_" + System.currentTimeMillis());
            }
            
            // 如果没有名称，使用KEY作为名称
            if (definition.getDefinitionName() == null || definition.getDefinitionName().isEmpty()) {
                definition.setDefinitionName(definition.getDefinitionKey());
            }
            
            // 设置默认值
            if (definition.getIsActive() == null) {
                definition.setIsActive(1L);
            }
            
            // 设置创建用户（导入操作者）
            if (createUser != null && !createUser.isEmpty()) {
                definition.setCreateUser(createUser);
            }
            
            // 保存流程定义
            boolean success = processDefinitionMapper.insert(definition) > 0;
            if (success) {
                return definition;
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("导入流程定义失败：" + e.getMessage(), e);
        }
    }

    @Override
    public List<ProcessNodeInfo> getProcessNodes(Long definitionId) {
        System.out.println("=== ProcessDefinitionServiceImpl.getProcessNodes ===");
        System.out.println("definitionId: " + definitionId);
        List<ProcessNodeInfo> nodes = new ArrayList<>();
        
        try {
            ProcessDefinition definition = processDefinitionMapper.selectById(definitionId);
            if (definition == null) {
                System.err.println("流程定义不存在，definitionId: " + definitionId);
                return nodes;
            }
            System.out.println("找到流程定义: " + definition.getDefinitionName());
            System.out.println("processXml是否为空: " + (definition.getProcessXml() == null || definition.getProcessXml().trim().isEmpty()));
            System.out.println("processJson是否为空: " + (definition.getProcessJson() == null || definition.getProcessJson().trim().isEmpty()));
            
            // 优先从processJson中解析自定义属性
            Map<String, Map<String, Object>> customProperties = null;
            if (definition.getProcessJson() != null && !definition.getProcessJson().trim().isEmpty()) {
                try {
                    System.out.println("开始解析processJson...");
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode jsonNode = objectMapper.readTree(definition.getProcessJson());
                    if (jsonNode.has("customProperties")) {
                        System.out.println("找到customProperties节点");
                        JsonNode customPropsNode = jsonNode.get("customProperties");
                        final Map<String, Map<String, Object>> finalCustomProperties = new HashMap<>();
                        customPropsNode.fields().forEachRemaining(entry -> {
                            Map<String, Object> props = new HashMap<>();
                            entry.getValue().fields().forEachRemaining(prop -> {
                                JsonNode propValue = prop.getValue();
                                if (propValue.isTextual()) {
                                    props.put(prop.getKey(), propValue.asText());
                                } else if (propValue.isNumber()) {
                                    if (propValue.isInt()) {
                                        props.put(prop.getKey(), propValue.asInt());
                                    } else {
                                        props.put(prop.getKey(), propValue.asLong());
                                    }
                                } else if (propValue.isBoolean()) {
                                    props.put(prop.getKey(), propValue.asBoolean() ? 1 : 0);
                                } else {
                                    props.put(prop.getKey(), propValue.asText());
                                }
                            });
                            finalCustomProperties.put(entry.getKey(), props);
                            System.out.println("解析节点 " + entry.getKey() + " 的自定义属性，属性数量: " + props.size());
                        });
                        customProperties = finalCustomProperties;
                        System.out.println("processJson解析完成，自定义属性节点数: " + finalCustomProperties.size());
                    } else {
                        System.out.println("processJson中没有customProperties节点");
                    }
                } catch (Exception e) {
                    System.err.println("解析processJson失败: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("processJson为空，跳过JSON解析");
            }
            
            // 解析XML获取节点基本信息
            if (definition.getProcessXml() != null && !definition.getProcessXml().trim().isEmpty()) {
                try {
                    System.out.println("开始解析processXml...");
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    factory.setNamespaceAware(true);
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document doc = builder.parse(new org.xml.sax.InputSource(new StringReader(definition.getProcessXml())));
                    
                    System.out.println("XML解析成功，开始提取节点...");
                    // 解析所有节点
                    parseBpmnNodes(doc, nodes, customProperties);
                    System.out.println("XML节点解析完成，找到节点数: " + nodes.size());
                    
                } catch (Exception e) {
                    System.err.println("解析processXml失败: " + e.getMessage());
                    e.printStackTrace();
                    // 抛出异常，让Controller层处理
                    throw new RuntimeException("解析流程XML失败: " + e.getMessage(), e);
                }
            } else {
                System.out.println("processXml为空，跳过XML解析");
            }
            
        } catch (RuntimeException e) {
            // 重新抛出RuntimeException
            throw e;
        } catch (Exception e) {
            System.err.println("获取流程节点失败: " + e.getMessage());
            e.printStackTrace();
            // 将其他异常包装为RuntimeException
            throw new RuntimeException("获取流程节点失败: " + e.getMessage(), e);
        }
        
        System.out.println("最终返回节点数量: " + nodes.size());
        System.out.println("=== ProcessDefinitionServiceImpl.getProcessNodes完成 ===");
        return nodes;
    }
    
    /**
     * 解析BPMN XML中的节点（支持命名空间）
     */
    private void parseBpmnNodes(Document doc, List<ProcessNodeInfo> nodes, Map<String, Map<String, Object>> customProperties) {
        System.out.println("=== parseBpmnNodes开始 ===");
        // 获取所有元素，包括带命名空间的
        NodeList allElements = doc.getElementsByTagName("*");
        System.out.println("XML中总元素数: " + allElements.getLength());
        int userTaskCount = 0, gatewayCount = 0, sequenceFlowCount = 0, otherTaskCount = 0;
        
        for (int i = 0; i < allElements.getLength(); i++) {
            Element element = (Element) allElements.item(i);
            String localName = element.getLocalName();
            if (localName == null) {
                // 如果没有localName，尝试从tagName中提取（去掉命名空间前缀）
                String tagName = element.getTagName();
                int colonIndex = tagName.indexOf(':');
                localName = colonIndex >= 0 ? tagName.substring(colonIndex + 1) : tagName;
            }
            
            if ("userTask".equals(localName)) {
                userTaskCount++;
                ProcessNodeInfo nodeInfo = createUserTaskNodeInfo(element, customProperties);
                if (nodeInfo != null) {
                    nodes.add(nodeInfo);
                    System.out.println("  添加userTask节点: " + nodeInfo.getId() + " - " + nodeInfo.getName());
                }
            } else if ("serviceTask".equals(localName) || "scriptTask".equals(localName) ||
                       "sendTask".equals(localName) || "receiveTask".equals(localName) ||
                       "manualTask".equals(localName) || "businessRuleTask".equals(localName)) {
                otherTaskCount++;
                ProcessNodeInfo nodeInfo = createBasicNodeInfo(element, localName);
                if (nodeInfo != null) {
                    nodes.add(nodeInfo);
                    System.out.println("  添加" + localName + "节点: " + nodeInfo.getId() + " - " + nodeInfo.getName());
                }
            } else if ("exclusiveGateway".equals(localName) || "parallelGateway".equals(localName) ||
                       "inclusiveGateway".equals(localName)) {
                gatewayCount++;
                ProcessNodeInfo nodeInfo = createGatewayNodeInfo(element, localName);
                if (nodeInfo != null) {
                    nodes.add(nodeInfo);
                    System.out.println("  添加" + localName + "节点: " + nodeInfo.getId() + " - " + nodeInfo.getName());
                }
            }
            // 注意：sequenceFlow不添加到nodes列表，只统计
            if ("sequenceFlow".equals(localName)) {
                sequenceFlowCount++;
            }
        }
        System.out.println("解析统计 - userTask: " + userTaskCount + ", gateway: " + gatewayCount + ", sequenceFlow: " + sequenceFlowCount + ", otherTask: " + otherTaskCount);
        System.out.println("=== parseBpmnNodes完成 ===");
    }
    
    /**
     * 创建用户任务节点信息
     */
    private ProcessNodeInfo createUserTaskNodeInfo(Element task, Map<String, Map<String, Object>> customProperties) {
        ProcessNodeInfo nodeInfo = new ProcessNodeInfo();
        String id = task.getAttribute("id");
        String name = task.getAttribute("name");
        
        nodeInfo.setId(id);
        nodeInfo.setName(name != null ? name : "");
        nodeInfo.setType("userTask");
        
        // 先从XML中获取assignee属性（如果存在）
        String xmlAssignee = task.getAttribute("assignee");
        if (xmlAssignee != null && !xmlAssignee.trim().isEmpty()) {
            // 如果XML中有assignee，但没有customProperties中的assigneeName，使用assignee作为名称
            nodeInfo.setAssigneeId(xmlAssignee);
            nodeInfo.setAssigneeName(xmlAssignee); // 临时使用，后续可能被customProperties覆盖
        }
        
        // 从customProperties中获取自定义属性
        Map<String, Object> props = customProperties != null ? customProperties.get(id) : null;
        
        if (props != null) {
            // 审批类型
            if (props.containsKey("approvalType")) {
                nodeInfo.setApprovalType(String.valueOf(props.get("approvalType")));
            }
            
            // 审批人配置
            if (props.containsKey("assigneeType")) {
                String assigneeType = String.valueOf(props.get("assigneeType"));
                nodeInfo.setAssigneeType(assigneeType);
                nodeInfo.setAssigneeTypeText(getAssigneeTypeText(assigneeType));
            }
            if (props.containsKey("assigneeId")) {
                nodeInfo.setAssigneeId(String.valueOf(props.get("assigneeId")));
            }
            if (props.containsKey("assigneeName")) {
                nodeInfo.setAssigneeName(String.valueOf(props.get("assigneeName")));
            }
            if (props.containsKey("assigneeCode")) {
                nodeInfo.setAssigneeCode(String.valueOf(props.get("assigneeCode")));
            }
            if (props.containsKey("positionCode")) {
                String positionCode = String.valueOf(props.get("positionCode"));
                nodeInfo.setPositionCode(positionCode);
                // 如果审批人类型是指定岗位但没有assigneeName，使用岗位代码作为显示名称
                if ("position".equals(nodeInfo.getAssigneeType()) && 
                    (nodeInfo.getAssigneeName() == null || nodeInfo.getAssigneeName().isEmpty())) {
                    nodeInfo.setAssigneeName("岗位:" + positionCode);
                }
            }
            if (props.containsKey("deptCode")) {
                nodeInfo.setDeptCode(String.valueOf(props.get("deptCode")));
            }
            if (props.containsKey("responsibleType")) {
                String responsibleType = String.valueOf(props.get("responsibleType"));
                nodeInfo.setResponsibleType(responsibleType);
                // 如果审批人类型是responsible，根据responsibleType设置审批人名称
                if ("responsible".equals(nodeInfo.getAssigneeType())) {
                    String responsibleTypeText = getResponsibleTypeText(responsibleType);
                    if (responsibleTypeText != null && !responsibleTypeText.isEmpty()) {
                        nodeInfo.setAssigneeName(responsibleTypeText);
                    }
                }
            }
            
            // 打印配置
            if (props.containsKey("needPrint")) {
                Object value = props.get("needPrint");
                nodeInfo.setNeedPrint(value instanceof Number ? ((Number) value).intValue() : (Boolean.parseBoolean(String.valueOf(value)) ? 1 : 0));
            }
            if (props.containsKey("printOrder")) {
                Object value = props.get("printOrder");
                nodeInfo.setPrintOrder(value instanceof Number ? ((Number) value).intValue() : Integer.parseInt(String.valueOf(value)));
            }
            
            // 超时配置
            if (props.containsKey("enableTimeout")) {
                Object value = props.get("enableTimeout");
                nodeInfo.setEnableTimeout(value instanceof Number ? ((Number) value).intValue() : (Boolean.parseBoolean(String.valueOf(value)) ? 1 : 0));
            }
            if (props.containsKey("timeoutHours")) {
                Object value = props.get("timeoutHours");
                nodeInfo.setTimeoutHours(value instanceof Number ? ((Number) value).intValue() : Integer.parseInt(String.valueOf(value)));
            }
            if (props.containsKey("timeoutAction")) {
                nodeInfo.setTimeoutAction(String.valueOf(props.get("timeoutAction")));
            }
            
            // 高级配置
            if (props.containsKey("allowAddsign")) {
                Object value = props.get("allowAddsign");
                nodeInfo.setAllowAddsign(value instanceof Number ? ((Number) value).intValue() : (Boolean.parseBoolean(String.valueOf(value)) ? 1 : 0));
            }
            if (props.containsKey("allowTransfer")) {
                Object value = props.get("allowTransfer");
                nodeInfo.setAllowTransfer(value instanceof Number ? ((Number) value).intValue() : (Boolean.parseBoolean(String.valueOf(value)) ? 1 : 0));
            }
            if (props.containsKey("allowReject")) {
                Object value = props.get("allowReject");
                nodeInfo.setAllowReject(value instanceof Number ? ((Number) value).intValue() : (Boolean.parseBoolean(String.valueOf(value)) ? 1 : 0));
            }
            if (props.containsKey("rejectStrategy")) {
                nodeInfo.setRejectStrategy(String.valueOf(props.get("rejectStrategy")));
            }
            
            // 会签配置
            if (props.containsKey("multiInstanceType")) {
                nodeInfo.setMultiInstanceType(String.valueOf(props.get("multiInstanceType")));
            }
            if (props.containsKey("multiInstanceCount")) {
                Object count = props.get("multiInstanceCount");
                nodeInfo.setMultiInstanceCount(count instanceof Number ? ((Number) count).intValue() : Integer.parseInt(String.valueOf(count)));
            }
            if (props.containsKey("completionCondition")) {
                Object condition = props.get("completionCondition");
                nodeInfo.setCompletionCondition(condition instanceof Number ? ((Number) condition).intValue() : Integer.parseInt(String.valueOf(condition)));
            }
            
            // 节点描述
            if (props.containsKey("description")) {
                nodeInfo.setDescription(String.valueOf(props.get("description")));
            }
        } else {
            // 如果customProperties中没有该节点的信息，尝试从XML属性推断
            // 例如，如果assignee是${initiator}，则可能是发起人
            if (xmlAssignee != null && !xmlAssignee.trim().isEmpty()) {
                if (xmlAssignee.contains("${initiator}") || xmlAssignee.contains("initiator")) {
                    nodeInfo.setAssigneeType("initiator");
                    nodeInfo.setAssigneeTypeText("发起人");
                    nodeInfo.setAssigneeName("发起人");
                } else if (xmlAssignee.contains("${deptManager}") || xmlAssignee.contains("deptManager")) {
                    nodeInfo.setAssigneeType("dept");
                    nodeInfo.setAssigneeTypeText("部门负责人");
                    nodeInfo.setAssigneeName("部门负责人");
                } else {
                    // 其他情况，可能是具体的用户名
                    nodeInfo.setAssigneeType("user");
                    nodeInfo.setAssigneeTypeText("指定用户");
                    // 如果assignee看起来像是一个具体的值（不是表达式），使用它作为名称
                    if (!xmlAssignee.startsWith("${") && !xmlAssignee.contains("$")) {
                        nodeInfo.setAssigneeName(xmlAssignee);
                    }
                }
            }
        }
        
        // 如果还是没有审批人名称，根据审批人类型设置默认显示
        if ((nodeInfo.getAssigneeName() == null || nodeInfo.getAssigneeName().isEmpty()) && 
            nodeInfo.getAssigneeType() != null) {
            String assigneeType = nodeInfo.getAssigneeType();
            switch (assigneeType) {
                case "initiator":
                    nodeInfo.setAssigneeName("发起人");
                    break;
                case "dept":
                    nodeInfo.setAssigneeName("部门负责人");
                    break;
                case "manage_dept":
                    nodeInfo.setAssigneeName("归口审批人");
                    break;
                case "position":
                    // 岗位类型会在上面处理positionCode时设置
                    if (nodeInfo.getPositionCode() != null && !nodeInfo.getPositionCode().isEmpty()) {
                        nodeInfo.setAssigneeName("岗位:" + nodeInfo.getPositionCode());
                    }
                    break;
                case "responsible":
                    // 负责人类型会在上面处理responsibleType时设置
                    break;
                default:
                    // 其他类型保持为空，前端会显示"-"
                    break;
            }
        }
        
        return nodeInfo;
    }
    
    /**
     * 创建基本节点信息（非用户任务）
     */
    private ProcessNodeInfo createBasicNodeInfo(Element element, String type) {
        ProcessNodeInfo nodeInfo = new ProcessNodeInfo();
        nodeInfo.setId(element.getAttribute("id"));
        nodeInfo.setName(element.getAttribute("name"));
        nodeInfo.setType(type);
        return nodeInfo;
    }
    
    /**
     * 创建网关节点信息
     */
    private ProcessNodeInfo createGatewayNodeInfo(Element gateway, String type) {
        ProcessNodeInfo nodeInfo = new ProcessNodeInfo();
        nodeInfo.setId(gateway.getAttribute("id"));
        nodeInfo.setName(gateway.getAttribute("name"));
        nodeInfo.setType(type);
        // 可以从customProperties中获取gatewayType
        return nodeInfo;
    }
    
    /**
     * 创建连线节点信息
     */
    private ProcessNodeInfo createSequenceFlowNodeInfo(Element flow, Map<String, Map<String, Object>> customProperties) {
        ProcessNodeInfo nodeInfo = new ProcessNodeInfo();
        String id = flow.getAttribute("id");
        String name = flow.getAttribute("name");
        
        nodeInfo.setId(id);
        nodeInfo.setName(name != null ? name : "");
        nodeInfo.setType("sequenceFlow");
        
        // 解析条件表达式（支持命名空间）
        NodeList allChildElements = flow.getChildNodes();
        for (int i = 0; i < allChildElements.getLength(); i++) {
            if (allChildElements.item(i) instanceof Element) {
                Element childElement = (Element) allChildElements.item(i);
                String localName = childElement.getLocalName();
                if (localName == null) {
                    String tagName = childElement.getTagName();
                    int colonIndex = tagName.indexOf(':');
                    localName = colonIndex >= 0 ? tagName.substring(colonIndex + 1) : tagName;
                }
                if ("conditionExpression".equals(localName)) {
                    String conditionText = childElement.getTextContent();
                    if (conditionText != null && !conditionText.trim().isEmpty()) {
                        nodeInfo.setConditionExpression(conditionText.trim());
                    }
                    break;
                }
            }
        }
        
        // 从customProperties中获取条件配置
        Map<String, Object> props = customProperties != null ? customProperties.get(id) : null;
        if (props != null) {
            if (props.containsKey("conditionType")) {
                nodeInfo.setConditionType(String.valueOf(props.get("conditionType")));
            }
            if (props.containsKey("conditionExpression") && nodeInfo.getConditionExpression() == null) {
                nodeInfo.setConditionExpression(String.valueOf(props.get("conditionExpression")));
            }
        }
        
        return nodeInfo;
    }
    
    /**
     * 获取审批人类型文本
     */
    private String getAssigneeTypeText(String assigneeType) {
        if (assigneeType == null) {
            return "";
        }
        switch (assigneeType) {
            case "user":
                return "指定用户";
            case "position":
                return "指定岗位";
            case "dept":
                return "部门负责人";
            case "manage_dept":
                return "归口审批人";
            case "initiator":
                return "发起人";
            case "previous":
                return "上一节点审批人";
            case "responsible":
                return "负责人";
            default:
                return assigneeType;
        }
    }
    
    /**
     * 获取负责人类型文本
     */
    private String getResponsibleTypeText(String responsibleType) {
        if (responsibleType == null) {
            return null;
        }
        switch (responsibleType) {
            case "DEPT_MANAGER":
                return "发起人部门负责人";
            case "NURSE_MANAGER":
                return "发起人部门护士长";
            case "VICE_PRESIDENT":
                return "发起人部门分管院长";
            default:
                return responsibleType;
        }
    }
    
    /**
     * 解析流程定义并返回节点信息列表（根据业务数据动态获取审批人）
     */
    @Override
    public List<ProcessNodeInfo> getProcessNodesWithBusiness(Long definitionId, String applyNo) {
        // 先获取基础节点信息
        List<ProcessNodeInfo> nodes = getProcessNodes(definitionId);
        
        if (nodes == null || nodes.isEmpty() || applyNo == null || applyNo.trim().isEmpty()) {
            return nodes;
        }
        
        // 获取申请单信息（支持预算申请、报账申请、采购申请、HR业务申请和合同）
        BudgetApply budgetApply = null;
        CtrlPayout ctrlPayout = null;
        AssetPurchaseApplyMain assetPurchaseApply = null;
        com.hrp.common.entity.HrApply hrApply = null;
        PactMain pactMain = null;
        String businessType = null;
        
        // 先尝试获取预算申请
        if (budgServiceClient != null) {
            try {
                Result<BudgetApply> result = budgServiceClient.getBudgetApplyByNo(applyNo);
                if (result != null && result.getCode() == 200 && result.getData() != null) {
                    budgetApply = result.getData();
                    // 从模板设置表获取business_type
                    if (budgetApply.getTemplateConfigId() != null && templateConfigService != null) {
                        TemplateConfig templateConfig = templateConfigService.getById(budgetApply.getTemplateConfigId());
                        if (templateConfig != null && templateConfig.getBusinessType() != null) {
                            businessType = templateConfig.getBusinessType();
                        }
                    }
                    // 如果无法从模板设置表获取，使用备用值
                    if (businessType == null || businessType.isEmpty()) {
                        businessType = "BUDGET_APPLY"; // 备用值
                    }
                }
            } catch (Exception e) {
                // 忽略异常，继续尝试报账申请
            }
        }
        
        // 如果预算申请不存在，尝试获取报账申请
        if (budgetApply == null && reimbServiceClient != null) {
            try {
                Result<CtrlPayout> result = reimbServiceClient.getCtrlPayoutByBillcode(applyNo);
                if (result != null && result.getCode() == 200 && result.getData() != null) {
                    ctrlPayout = result.getData();
                    // 从模板设置表获取business_type
                    if (ctrlPayout.getTemplateConfigId() != null && templateConfigService != null) {
                        TemplateConfig templateConfig = templateConfigService.getById(ctrlPayout.getTemplateConfigId());
                        if (templateConfig != null && templateConfig.getBusinessType() != null) {
                            businessType = templateConfig.getBusinessType();
                        }
                    }
                    // 如果无法从模板设置表获取，使用备用值
                    if (businessType == null || businessType.isEmpty()) {
                        businessType = "PAYOUT_APPLY"; // 备用值
                    }
                }
            } catch (Exception e) {
                System.err.println("调用报账服务获取申请单信息失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // 如果预算申请和报账申请都不存在，尝试获取采购申请
        if (budgetApply == null && ctrlPayout == null && assetServiceClient != null) {
            try {
                Result<AssetPurchaseApplyMain> result = assetServiceClient.getAssetPurchaseApplyByNo(applyNo);
                if (result != null && result.getCode() == 200 && result.getData() != null) {
                    assetPurchaseApply = result.getData();
                    // 从模板设置表获取business_type
                    if (assetPurchaseApply.getTemplateConfigId() != null && templateConfigService != null) {
                        TemplateConfig templateConfig = templateConfigService.getById(assetPurchaseApply.getTemplateConfigId());
                        if (templateConfig != null && templateConfig.getBusinessType() != null) {
                            businessType = templateConfig.getBusinessType();
                        }
                    }
                    // 如果无法从模板设置表获取，使用备用值
                    if (businessType == null || businessType.isEmpty()) {
                        businessType = "ASSET_PURCHASE_APPLY"; // 备用值
                    }
                }
            } catch (Exception e) {
                System.err.println("调用采购服务获取申请单信息失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // 如果预算申请、报账申请和采购申请都不存在，尝试获取HR业务申请
        if (budgetApply == null && ctrlPayout == null && assetPurchaseApply == null && hrServiceClient != null) {
            try {
                Result<com.hrp.common.entity.HrApply> result = hrServiceClient.getHrApplyByApplyNo(applyNo);
                if (result != null && result.getCode() == 200 && result.getData() != null) {
                    hrApply = result.getData();
                    // 从模板设置表获取business_type
                    if (hrApply.getTemplateConfigId() != null && templateConfigService != null) {
                        TemplateConfig templateConfig = templateConfigService.getById(hrApply.getTemplateConfigId());
                        if (templateConfig != null && templateConfig.getBusinessType() != null) {
                            businessType = templateConfig.getBusinessType();
                        }
                    }
                    // 如果无法从模板设置表获取，使用备用值
                    if (businessType == null || businessType.isEmpty()) {
                        businessType = "HR_APPLY"; // 备用值
                    }
                }
            } catch (Exception e) {
                System.err.println("调用HR服务获取业务申请信息失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // 如果预算申请、报账申请、采购申请和HR业务申请都不存在，尝试获取合同
        if (budgetApply == null && ctrlPayout == null && assetPurchaseApply == null && hrApply == null && contractServiceClient != null) {
            try {
                Result<PactMain> result = contractServiceClient.getContractByContractNo(applyNo);
                if (result != null && result.getCode() == 200 && result.getData() != null) {
                    pactMain = result.getData();
                    // 从模板设置表获取business_type
                    if (pactMain.getTemplateConfigId() != null && templateConfigService != null) {
                        TemplateConfig templateConfig = templateConfigService.getById(pactMain.getTemplateConfigId());
                        if (templateConfig != null && templateConfig.getBusinessType() != null) {
                            businessType = templateConfig.getBusinessType();
                        }
                    }
                    // 如果无法从模板设置表获取，使用备用值
                    if (businessType == null || businessType.isEmpty()) {
                        businessType = "CONTRACT"; // 备用值
                    }
                }
            } catch (Exception e) {
                System.err.println("调用合同服务获取合同信息失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        if (budgetApply == null && ctrlPayout == null && assetPurchaseApply == null && hrApply == null && pactMain == null) {
            return nodes;
        }
        
        // 提取申请单关键信息（统一处理预算申请、报账申请、采购申请、HR业务申请和合同）
        Long applicantId = null;
        String applicantName = null;
        String applicantCode = null;
        Long deptId = null;
        String deptCode = null;
        Long subjectId = null;
        
        if (budgetApply != null) {
            applicantId = budgetApply.getApplicantId();
            applicantName = budgetApply.getApplicantName();
            applicantCode = budgetApply.getApplicantCode();
            deptId = budgetApply.getDeptId();
            deptCode = budgetApply.getDeptCode();
            subjectId = budgetApply.getSubjectId();
        } else if (ctrlPayout != null) {
            applicantId = ctrlPayout.getEmpId();
            applicantName = ctrlPayout.getEmpName();
            applicantCode = ctrlPayout.getEmpCode();
            deptId = ctrlPayout.getDeptId();
            // 报账申请没有subjectId，但可以从budget_detail中获取（如果需要）
            subjectId = null;
            
            System.out.println("=== 从报账申请获取信息 ===");
            System.out.println("申请人ID: " + applicantId);
            System.out.println("申请人姓名: " + applicantName);
            System.out.println("申请人工号: " + applicantCode);
            System.out.println("部门ID: " + deptId);
            System.out.println("部门名称: " + ctrlPayout.getDeptName());
        } else if (assetPurchaseApply != null) {
            applicantId = assetPurchaseApply.getApplyEmpId();
            // 直接使用查询时已获取的申请人姓名和工号
            applicantName = assetPurchaseApply.getApplyEmpName();
            applicantCode = assetPurchaseApply.getApplyEmpCode();
            
            // 直接使用查询时已获取的部门名称和编码
            deptCode = assetPurchaseApply.getApplyDeptCode();
            
            // AssetPurchaseApplyMain的applyDeptId是String类型，可能是部门ID（数字字符串）或部门编码
            if (assetPurchaseApply.getApplyDeptId() != null && !assetPurchaseApply.getApplyDeptId().isEmpty()) {
                try {
                    // 尝试解析为Long类型的部门ID
                    deptId = Long.parseLong(assetPurchaseApply.getApplyDeptId());
                } catch (NumberFormatException e) {
                    // 如果无法解析为Long，说明是部门编码，已经在上面设置了deptCode
                    if (deptCode == null || deptCode.isEmpty()) {
                        deptCode = assetPurchaseApply.getApplyDeptId();
                    }
                }
            }
            
            // 如果部门编码已获取但部门ID为空，尝试通过部门编码查询部门ID
            if (deptId == null && deptCode != null && !deptCode.isEmpty() && deptService != null) {
                try {
                    Dept dept = deptService.getByCode(deptCode);
                    if (dept != null && dept.getDeptId() != null) {
                        deptId = dept.getDeptId();
                    }
                } catch (Exception e) {
                    System.err.println("根据部门编码获取部门ID失败: " + e.getMessage());
                }
            }
            
            // 如果申请人工号为空，尝试通过员工ID查询员工信息获取工号
            if ((applicantCode == null || applicantCode.isEmpty()) && applicantId != null && userEmployeeService != null) {
                try {
                    // 先通过员工ID从用户信息中查找（通过empId关联）
                    // 但UserEmployeeService只有getEmployeeByCode方法，需要通过empId查询员工
                    // 这里我们可以通过员工ID查询员工信息（但需要先添加这个方法）
                    // 暂时先跳过，因为通常applyEmpCode已经通过JOIN查询获取了
                } catch (Exception e) {
                    System.err.println("根据员工ID获取员工信息失败: " + e.getMessage());
                }
            }
            
            // 如果申请人姓名或工号仍为空，尝试通过工号查询用户信息
            if ((applicantName == null || applicantName.isEmpty() || applicantCode == null || applicantCode.isEmpty()) 
                && applicantCode != null && !applicantCode.isEmpty() && userService != null) {
                try {
                    com.hrp.common.entity.User user = userService.getByAccount(applicantCode);
                    if (user != null) {
                        if (applicantName == null || applicantName.isEmpty()) {
                            applicantName = user.getName();
                        }
                        // 如果部门信息仍为空，尝试从用户信息获取
                        if (deptId == null && (deptCode == null || deptCode.isEmpty())) {
                            if (user.getDeptCode() != null && !user.getDeptCode().isEmpty()) {
                                deptCode = user.getDeptCode();
                            } else if (user.getDeptId() != null) {
                                deptId = user.getDeptId();
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("根据申请人工号获取用户信息失败: " + e.getMessage());
                }
            }
            
            // 采购申请没有subjectId
            subjectId = null;
            
            System.out.println("=== 从采购申请获取信息 ===");
            System.out.println("申请人ID: " + applicantId);
            System.out.println("申请人姓名: " + applicantName);
            System.out.println("申请人工号: " + applicantCode);
            System.out.println("部门ID: " + deptId);
            System.out.println("部门编码: " + deptCode);
            System.out.println("部门名称: " + assetPurchaseApply.getApplyDeptName());
        } else if (hrApply != null) {
            // HR业务申请信息提取
            applicantId = hrApply.getEmpId();
            applicantName = hrApply.getEmpName();
            applicantCode = hrApply.getEmpCode();
            deptId = hrApply.getDeptId();
            // HR业务申请没有subjectId
            subjectId = null;
            
            System.out.println("=== 从HR业务申请获取信息 ===");
            System.out.println("申请人ID: " + applicantId);
            System.out.println("申请人姓名: " + applicantName);
            System.out.println("申请人工号: " + applicantCode);
            System.out.println("部门ID: " + deptId);
            System.out.println("部门名称: " + hrApply.getDeptName());
        } else if (pactMain != null) {
            // 合同信息提取
            applicantId = pactMain.getEmpId();
            // 合同使用empName和deptName（显示字段），需要根据empId或deptId查询
            applicantName = pactMain.getEmpName();
            deptId = pactMain.getDeptId();
            // 合同没有subjectId
            subjectId = null;
            
            System.out.println("=== 从合同获取信息 ===");
            System.out.println("申请人ID: " + applicantId);
            System.out.println("申请人姓名: " + applicantName);
            System.out.println("部门ID: " + deptId);
            System.out.println("部门名称: " + pactMain.getDeptName());
        }
        
        // 如果deptId为空，尝试根据applicantCode（工号）获取用户信息（优先使用，因为工号更可靠）
        if (deptId == null && applicantCode != null && !applicantCode.isEmpty() && userService != null) {
            try {
                com.hrp.common.entity.User user = userService.getByAccount(applicantCode);
                if (user != null) {
                    if (user.getDeptCode() != null && !user.getDeptCode().isEmpty()) {
                        // 根据部门编码获取部门信息
                        if (deptService != null) {
                            Dept deptByCode = deptService.getByCode(user.getDeptCode());
                            if (deptByCode != null && deptByCode.getDeptId() != null) {
                                deptId = deptByCode.getDeptId();
                                System.out.println("=== 根据申请人工号（通过sys_user）获取部门信息 ===");
                                System.out.println("申请人工号: " + applicantCode);
                                System.out.println("申请人姓名: " + applicantName);
                                System.out.println("部门编码: " + user.getDeptCode());
                                System.out.println("部门ID: " + deptId);
                            }
                        }
                    } else if (user.getDeptId() != null) {
                        // 如果用户表中有deptId字段，直接使用
                        deptId = user.getDeptId();
                        System.out.println("=== 根据申请人工号（从sys_user.deptId）获取部门信息 ===");
                        System.out.println("申请人工号: " + applicantCode);
                        System.out.println("部门ID: " + deptId);
                    }
                }
            } catch (Exception e) {
                System.err.println("根据申请人工号（通过sys_user）获取部门信息失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // 如果deptId仍为空，尝试根据applicantId（用户ID）获取用户的部门信息
        if (deptId == null && applicantId != null && userService != null) {
            try {
                com.hrp.common.entity.User user = userService.getById(String.valueOf(applicantId));
                if (user != null) {
                    if (user.getDeptCode() != null && !user.getDeptCode().isEmpty()) {
                        // 根据部门编码获取部门信息
                        if (deptService != null) {
                            Dept deptByCode = deptService.getByCode(user.getDeptCode());
                            if (deptByCode != null && deptByCode.getDeptId() != null) {
                                deptId = deptByCode.getDeptId();
                                System.out.println("=== 根据申请人ID获取部门信息 ===");
                                System.out.println("申请人ID: " + applicantId);
                                System.out.println("申请人姓名: " + applicantName);
                                System.out.println("部门编码: " + user.getDeptCode());
                                System.out.println("部门ID: " + deptId);
                            }
                        }
                    } else if (user.getDeptId() != null) {
                        // 如果用户表中有deptId字段，直接使用
                        deptId = user.getDeptId();
                        System.out.println("=== 根据申请人ID（从sys_user.deptId）获取部门信息 ===");
                        System.out.println("申请人ID: " + applicantId);
                        System.out.println("部门ID: " + deptId);
                    }
                }
            } catch (Exception e) {
                System.err.println("根据申请人ID获取部门信息失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // 如果仍然无法获取deptId，尝试根据applicantCode（工号）获取员工信息
        if (deptId == null && applicantCode != null && !applicantCode.isEmpty() && userEmployeeService != null) {
            try {
                Employee employee = userEmployeeService.getEmployeeByCode(applicantCode);
                if (employee != null && employee.getDeptId() != null) {
                    deptId = employee.getDeptId();
                    System.out.println("=== 根据申请人工号（通过sys_emp）获取部门信息 ===");
                    System.out.println("申请人工号: " + applicantCode);
                    System.out.println("申请人姓名: " + applicantName);
                    System.out.println("部门ID: " + deptId);
                }
            } catch (Exception e) {
                System.err.println("根据申请人工号（通过sys_emp）获取部门信息失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // 获取部门信息（用于查询部门负责人等）
        Dept dept = null;
        if (deptId != null && deptService != null) {
            try {
                dept = deptService.getById(deptId);
                if (dept != null) {
                    System.out.println("=== 获取部门信息成功 ===");
                    System.out.println("部门ID: " + deptId);
                    System.out.println("部门名称: " + dept.getDeptName());
                    System.out.println("部门负责人: " + dept.getDeptManagerName());
                    System.out.println("分管院长: " + dept.getVicePresidentName());
                } else {
                    System.err.println("获取部门信息失败: 部门ID=" + deptId + "，查询结果为空");
                }
            } catch (Exception e) {
                System.err.println("获取部门信息失败: 部门ID=" + deptId + "，错误=" + e.getMessage());
                e.printStackTrace();
            }
        } else if (deptCode != null && !deptCode.isEmpty() && deptService != null) {
            // 如果deptId为空，但deptCode不为空，尝试通过deptCode获取部门信息
            try {
                dept = deptService.getByCode(deptCode);
                if (dept != null) {
                    deptId = dept.getDeptId(); // 更新deptId
                    System.out.println("=== 通过部门编码获取部门信息成功 ===");
                    System.out.println("部门编码: " + deptCode);
                    System.out.println("部门ID: " + deptId);
                    System.out.println("部门名称: " + dept.getDeptName());
                    System.out.println("部门负责人: " + dept.getDeptManagerName());
                    System.out.println("分管院长: " + dept.getVicePresidentName());
                } else {
                    System.err.println("获取部门信息失败: 部门编码=" + deptCode + "，查询结果为空");
                }
            } catch (Exception e) {
                System.err.println("获取部门信息失败: 部门编码=" + deptCode + "，错误=" + e.getMessage());
                e.printStackTrace();
            }
        } else {
            if (deptId == null && (deptCode == null || deptCode.isEmpty())) {
                System.err.println("无法获取部门信息: deptId和deptCode都为空");
            }
            if (deptService == null) {
                System.err.println("无法获取部门信息: deptService未注入");
            }
        }
        
        // 获取归口审批人信息（从预算主体获取，仅预算申请有）
        String manageEmpName = null;
        Long manageEmpId = null;
        String manageEmpCode = null;
        if (subjectId != null && budgServiceClient != null) {
            try {
                com.hrp.common.entity.Result<BudgetSubject> result = budgServiceClient.getBudgetSubjectById(subjectId);
                if (result != null && result.getCode() == 200 && result.getData() != null) {
                    BudgetSubject subject = result.getData();
                    manageEmpName = subject.getManageEmpName();
                    manageEmpId = subject.getManageEmpId();
                    manageEmpCode = subject.getManageEmpCode();
                }
            } catch (Exception e) {
                System.err.println("获取归口审批人信息失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // 根据条件表达式过滤节点，只返回符合条件的路径上的节点
        ProcessDefinition definition = processDefinitionMapper.selectById(definitionId);
        if (definition != null && definition.getProcessXml() != null && !definition.getProcessXml().trim().isEmpty()) {
            if (budgetApply != null) {
                nodes = filterNodesByConditions(nodes, definition.getProcessXml(), budgetApply);
            } else if (ctrlPayout != null) {
                // 报账申请的条件过滤
                nodes = filterNodesByConditions(nodes, definition.getProcessXml(), ctrlPayout);
            } else if (assetPurchaseApply != null) {
                // 采购申请的条件过滤
                nodes = filterNodesByConditions(nodes, definition.getProcessXml(), assetPurchaseApply);
            } else if (hrApply != null) {
                // HR业务申请的条件过滤
                nodes = filterNodesByConditions(nodes, definition.getProcessXml(), hrApply);
            } else if (pactMain != null) {
                // 合同的条件过滤
                nodes = filterNodesByConditions(nodes, definition.getProcessXml(), pactMain);
            }
        }
        
        // 遍历节点，动态设置审批人
        for (ProcessNodeInfo node : nodes) {
            if (node.getAssigneeType() == null) {
                continue;
            }
            
            String assigneeType = node.getAssigneeType();
            String assigneeName = null;
            String assigneeId = null;
            String assigneeCode = null;
            
            switch (assigneeType) {
                case "initiator":
                    // 发起人 -> 申请人姓名、ID、Code
                    if (applicantName != null && !applicantName.trim().isEmpty()) {
                        assigneeName = applicantName;
                    }
                    assigneeCode = applicantCode;
                    // 通过工号查询 sys_user 的 id（如果 applicantId 不存在或者是 emp_id，需要通过工号查询）
                    if (assigneeCode != null && !assigneeCode.trim().isEmpty() && userService != null) {
                        try {
                            com.hrp.common.entity.User user = userService.getByAccount(assigneeCode);
                            if (user != null && user.getId() != null) {
                                assigneeId = user.getId(); // sys_user 的 id（String 类型）
                                System.out.println("发起人: 姓名=" + assigneeName + ", sys_user.id=" + assigneeId + ", Code=" + assigneeCode);
                            } else {
                                System.err.println("发起人: 根据工号 " + assigneeCode + " 未找到 sys_user 记录");
                            }
                        } catch (Exception e) {
                            System.err.println("查询发起人用户信息失败: empCode=" + assigneeCode + ", 错误=" + e.getMessage());
                        }
                    }
                    break;
                    
                case "responsible":
                    // 负责人类型，根据responsibleType获取
                    String responsibleType = node.getResponsibleType();
                    System.out.println("=== 解析负责人类型节点 ===");
                    System.out.println("节点名称: " + node.getName());
                    System.out.println("responsibleType: " + responsibleType);
                    System.out.println("dept是否为null: " + (dept == null));
                    
                    if (responsibleType != null && dept != null) {
                        switch (responsibleType) {
                            case "DEPT_MANAGER":
                                assigneeName = dept.getDeptManagerName();
                                assigneeCode = dept.getDeptManagerCode();
                                // 通过工号查询 sys_user 的 id
                                if (assigneeCode != null && !assigneeCode.trim().isEmpty() && userService != null) {
                                    try {
                                        com.hrp.common.entity.User user = userService.getByAccount(assigneeCode);
                                        if (user != null && user.getId() != null) {
                                            assigneeId = user.getId(); // sys_user 的 id（String 类型）
                                            System.out.println("部门负责人: 姓名=" + assigneeName + ", sys_user.id=" + assigneeId + ", Code=" + assigneeCode);
                                        } else {
                                            System.err.println("部门负责人: 根据工号 " + assigneeCode + " 未找到 sys_user 记录");
                                        }
                                    } catch (Exception e) {
                                        System.err.println("查询部门负责人用户信息失败: empCode=" + assigneeCode + ", 错误=" + e.getMessage());
                                    }
                                }
                                break;
                            case "NURSE_MANAGER":
                                assigneeName = dept.getNurseManagerName();
                                assigneeCode = dept.getNurseManagerCode();
                                // 通过工号查询 sys_user 的 id
                                if (assigneeCode != null && !assigneeCode.trim().isEmpty() && userService != null) {
                                    try {
                                        com.hrp.common.entity.User user = userService.getByAccount(assigneeCode);
                                        if (user != null && user.getId() != null) {
                                            assigneeId = user.getId(); // sys_user 的 id（String 类型）
                                            System.out.println("护士长: 姓名=" + assigneeName + ", sys_user.id=" + assigneeId + ", Code=" + assigneeCode);
                                        } else {
                                            System.err.println("护士长: 根据工号 " + assigneeCode + " 未找到 sys_user 记录");
                                        }
                                    } catch (Exception e) {
                                        System.err.println("查询护士长用户信息失败: empCode=" + assigneeCode + ", 错误=" + e.getMessage());
                                    }
                                }
                                break;
                            case "VICE_PRESIDENT":
                                assigneeName = dept.getVicePresidentName();
                                assigneeCode = dept.getVicePresidentCode();
                                // 通过工号查询 sys_user 的 id
                                if (assigneeCode != null && !assigneeCode.trim().isEmpty() && userService != null) {
                                    try {
                                        com.hrp.common.entity.User user = userService.getByAccount(assigneeCode);
                                        if (user != null && user.getId() != null) {
                                            assigneeId = user.getId(); // sys_user 的 id（String 类型）
                                            System.out.println("分管院长: 姓名=" + assigneeName + ", sys_user.id=" + assigneeId + ", Code=" + assigneeCode);
                                        } else {
                                            System.err.println("分管院长: 根据工号 " + assigneeCode + " 未找到 sys_user 记录");
                                        }
                                    } catch (Exception e) {
                                        System.err.println("查询分管院长用户信息失败: empCode=" + assigneeCode + ", 错误=" + e.getMessage());
                                    }
                                }
                                break;
                            default:
                                System.err.println("未知的responsibleType: " + responsibleType);
                                break;
                        }
                    } else {
                        if (responsibleType == null) {
                            System.err.println("节点 " + node.getName() + " 的responsibleType为空");
                        }
                        if (dept == null) {
                            System.err.println("节点 " + node.getName() + " 无法获取部门信息，deptId=" + deptId);
                        }
                    }
                    break;
                    
                case "manage_dept":
                    // 归口审批人 -> 从预算主体的归口负责人获取
                    if (manageEmpName != null && !manageEmpName.trim().isEmpty()) {
                        assigneeName = manageEmpName;
                    }
                    assigneeCode = manageEmpCode;
                    // 通过工号查询 sys_user 的 id
                    if (manageEmpCode != null && !manageEmpCode.trim().isEmpty() && userService != null) {
                        try {
                            com.hrp.common.entity.User user = userService.getByAccount(manageEmpCode);
                            if (user != null && user.getId() != null) {
                                assigneeId = user.getId(); // sys_user 的 id（String 类型）
                                System.out.println("归口审批人: 姓名=" + assigneeName + ", sys_user.id=" + assigneeId + ", Code=" + assigneeCode);
                            } else {
                                System.err.println("归口审批人: 根据工号 " + manageEmpCode + " 未找到 sys_user 记录");
                            }
                        } catch (Exception e) {
                            System.err.println("查询归口审批人用户信息失败: empCode=" + manageEmpCode + ", 错误=" + e.getMessage());
                        }
                    }
                    break;
                    
                case "user":
                    // 指定用户 -> 如果node中有assigneeCode，通过工号查询sys_user的id
                    if (node.getAssigneeCode() != null && !node.getAssigneeCode().isEmpty() && userService != null) {
                        try {
                            com.hrp.common.entity.User user = userService.getByAccount(node.getAssigneeCode());
                            if (user != null && user.getId() != null) {
                                assigneeId = user.getId(); // sys_user 的 id（String 类型）
                                System.out.println("指定用户: 姓名=" + node.getAssigneeName() + ", sys_user.id=" + assigneeId + ", Code=" + node.getAssigneeCode());
                            } else {
                                System.err.println("指定用户: 根据工号 " + node.getAssigneeCode() + " 未找到 sys_user 记录");
                            }
                        } catch (Exception e) {
                            System.err.println("查询指定用户信息失败: empCode=" + node.getAssigneeCode() + ", 错误=" + e.getMessage());
                        }
                    }
                    // 如果node中已经有assigneeId（且是sys_user的id格式），直接使用
                    if (assigneeId == null && node.getAssigneeId() != null && !node.getAssigneeId().isEmpty()) {
                        // 检查是否是UUID格式（sys_user的id通常是UUID）
                        if (node.getAssigneeId().matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}") || 
                            node.getAssigneeId().length() > 10) {
                            assigneeId = node.getAssigneeId();
                        } else {
                            // 如果是数字ID，可能是emp_id，需要通过其他方式查询
                            System.err.println("警告: 指定用户的assigneeId是数字格式(" + node.getAssigneeId() + ")，可能需要转换为sys_user.id");
                        }
                    }
                    break;
                    
                case "dept":
                    // 部门负责人 -> 根据节点中存储的deptCode获取指定部门的负责人
                    String selectedDeptCode = node.getDeptCode();
                    System.out.println("=== 解析部门负责人类型节点 ===");
                    System.out.println("节点名称: " + node.getName());
                    System.out.println("选择的部门编码: " + selectedDeptCode);
                    
                    if (selectedDeptCode != null && !selectedDeptCode.isEmpty() && deptService != null) {
                        try {
                            // 根据部门编码查询部门信息
                            Dept selectedDept = deptService.getByCode(selectedDeptCode);
                            if (selectedDept != null && selectedDept.getDeptManagerName() != null && 
                                !selectedDept.getDeptManagerName().isEmpty()) {
                                // 获取指定部门的负责人信息
                                assigneeName = selectedDept.getDeptManagerName();
                                assigneeCode = selectedDept.getDeptManagerCode();
                                System.out.println("找到部门负责人: 部门=" + selectedDept.getDeptName() + 
                                    ", 负责人姓名=" + assigneeName + ", 负责人工号=" + assigneeCode);
                                
                                // 通过工号查询 sys_user 的 id
                                if (assigneeCode != null && !assigneeCode.trim().isEmpty() && userService != null) {
                                    try {
                                        com.hrp.common.entity.User user = userService.getByAccount(assigneeCode);
                                        if (user != null && user.getId() != null) {
                                            assigneeId = user.getId(); // sys_user 的 id（String 类型）
                                            System.out.println("部门负责人: 姓名=" + assigneeName + ", sys_user.id=" + assigneeId + ", Code=" + assigneeCode);
                                        } else {
                                            System.err.println("部门负责人: 根据工号 " + assigneeCode + " 未找到 sys_user 记录");
                                        }
                                    } catch (Exception e) {
                                        System.err.println("查询部门负责人用户信息失败: empCode=" + assigneeCode + ", 错误=" + e.getMessage());
                                    }
                                }
                            } else {
                                if (selectedDept == null) {
                                    System.err.println("未找到部门编码为 " + selectedDeptCode + " 的部门");
                                } else {
                                    System.err.println("部门编码为 " + selectedDeptCode + " 的部门没有设置负责人");
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("根据部门编码获取部门负责人失败: deptCode=" + selectedDeptCode + ", 错误=" + e.getMessage());
                            e.printStackTrace();
                        }
                    } else {
                        if (selectedDeptCode == null || selectedDeptCode.isEmpty()) {
                            System.err.println("节点 " + node.getName() + " 的deptCode为空，无法获取部门负责人");
                        }
                        if (deptService == null) {
                            System.err.println("deptService未注入，无法查询部门信息");
                        }
                    }
                    break;
                    
                case "position":
                    // 指定岗位 -> 根据positionCode获取岗位名称
                    if (node.getPositionCode() != null && !node.getPositionCode().isEmpty() && positionService != null) {
                        try {
                            Position position = positionService.getByCode(node.getPositionCode());
                            if (position != null && position.getPositionName() != null && !position.getPositionName().isEmpty()) {
                                assigneeName = position.getPositionName();
                            } else {
                                // 如果获取不到岗位名称，使用岗位代码
                                assigneeName = node.getPositionCode();
                            }
                        } catch (Exception e) {
                            System.err.println("获取岗位信息失败: positionCode=" + node.getPositionCode() + ", 错误=" + e.getMessage());
                            e.printStackTrace();
                            // 如果获取失败，使用岗位代码作为显示名称
                            assigneeName = node.getPositionCode();
                        }
                    } else if (node.getPositionCode() != null && !node.getPositionCode().isEmpty()) {
                        // 如果positionService未注入，使用岗位代码作为显示名称
                        assigneeName = node.getPositionCode();
                    }
                    break;
            }
            
            // 如果获取到了动态审批人信息，更新节点信息
            if (assigneeName != null && !assigneeName.trim().isEmpty()) {
                node.setAssigneeName(assigneeName);
            } else {
                // 如果没有获取到审批人名称，检查当前节点的审批人名称
                String currentName = node.getAssigneeName();
                // 如果是描述性文字（如"发起人部门负责人"、"发起人部门分管院长"等），或者为空，则显示"待分配"
                if (currentName == null || currentName.trim().isEmpty() || 
                    currentName.contains("发起人") || currentName.contains("部门") || 
                    currentName.contains("审批人") || currentName.contains("护士长") || 
                    currentName.contains("分管")) {
                    node.setAssigneeName("待分配");
                }
            }
            
            // 设置审批人ID和Code
            if (assigneeId != null && !assigneeId.trim().isEmpty()) {
                node.setAssigneeId(assigneeId);
            }
            if (assigneeCode != null && !assigneeCode.trim().isEmpty()) {
                node.setAssigneeCode(assigneeCode);
            }
        }
        
        return nodes;
    }
    
    /**
     * 根据条件表达式过滤节点，只返回符合条件的路径上的节点（支持BudgetApply）
     */
    private List<ProcessNodeInfo> filterNodesByConditions(List<ProcessNodeInfo> nodes, String processXml, BudgetApply budgetApply) {
        System.out.println("=== filterNodesByConditions 开始 ===");
        System.out.println("原始节点数量: " + nodes.size());
        
        try {
            // 解析BPMN XML，构建节点连接关系
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new org.xml.sax.InputSource(new StringReader(processXml)));
            
            // 构建节点ID到节点的映射
            Map<String, ProcessNodeInfo> nodeMap = new HashMap<>();
            for (ProcessNodeInfo node : nodes) {
                nodeMap.put(node.getId(), node);
            }
            
            // 构建连接关系：sourceRef -> List<{targetRef, conditionExpression}>
            Map<String, List<FlowInfo>> flowMap = new HashMap<>();
            
            // 找到startEvent
            String startEventId = null;
            NodeList allElements = doc.getElementsByTagName("*");
            for (int i = 0; i < allElements.getLength(); i++) {
                Element element = (Element) allElements.item(i);
                String localName = element.getLocalName();
                if (localName == null) {
                    String tagName = element.getTagName();
                    int colonIndex = tagName.indexOf(':');
                    localName = colonIndex >= 0 ? tagName.substring(colonIndex + 1) : tagName;
                }
                
                if ("startEvent".equals(localName)) {
                    startEventId = element.getAttribute("id");
                    System.out.println("找到startEvent: " + startEventId);
                    break;
                }
            }
            
            // 解析所有sequenceFlow
            for (int i = 0; i < allElements.getLength(); i++) {
                Element element = (Element) allElements.item(i);
                String localName = element.getLocalName();
                if (localName == null) {
                    String tagName = element.getTagName();
                    int colonIndex = tagName.indexOf(':');
                    localName = colonIndex >= 0 ? tagName.substring(colonIndex + 1) : tagName;
                }
                
                if ("sequenceFlow".equals(localName)) {
                    String sourceRef = element.getAttribute("sourceRef");
                    String targetRef = element.getAttribute("targetRef");
                    String conditionExpression = null;
                    
                    // 提取条件表达式
                    NodeList childNodes = element.getChildNodes();
                    for (int j = 0; j < childNodes.getLength(); j++) {
                        if (childNodes.item(j) instanceof Element) {
                            Element childElement = (Element) childNodes.item(j);
                            String childLocalName = childElement.getLocalName();
                            if (childLocalName == null) {
                                String childTagName = childElement.getTagName();
                                int colonIndex = childTagName.indexOf(':');
                                childLocalName = colonIndex >= 0 ? childTagName.substring(colonIndex + 1) : childTagName;
                            }
                            if ("conditionExpression".equals(childLocalName)) {
                                conditionExpression = childElement.getTextContent();
                                break;
                            }
                        }
                    }
                    
                    if (sourceRef != null && targetRef != null) {
                        flowMap.computeIfAbsent(sourceRef, k -> new ArrayList<>()).add(new FlowInfo(targetRef, conditionExpression));
                        System.out.println("找到连线: " + sourceRef + " -> " + targetRef + (conditionExpression != null ? " (条件: " + conditionExpression + ")" : ""));
                    }
                }
            }
            
            if (startEventId == null) {
                System.out.println("未找到startEvent，返回所有节点");
                return nodes;
            }
            
            // 从业务对象中提取变量值
            Map<String, Object> variables = extractVariablesFromBudgetApply(budgetApply);
            System.out.println("提取的业务变量: " + variables);
            
            // 从startEvent开始，沿着满足条件的路径遍历
            Set<String> visitedNodes = new HashSet<>();
            Set<String> reachableNodes = new HashSet<>();
            traverseFromNode(startEventId, flowMap, variables, visitedNodes, reachableNodes);
            
            System.out.println("可达节点: " + reachableNodes);
            
            // 过滤节点，只保留可达的节点
            List<ProcessNodeInfo> filteredNodes = new ArrayList<>();
            for (ProcessNodeInfo node : nodes) {
                if (reachableNodes.contains(node.getId())) {
                    filteredNodes.add(node);
                }
            }
            
            // 按照流程执行顺序排序节点（拓扑排序）
            filteredNodes = sortNodesByFlowOrder(filteredNodes, flowMap, startEventId, variables);
            
            System.out.println("过滤后节点数量: " + filteredNodes.size());
            System.out.println("=== filterNodesByConditions 完成 ===");
            return filteredNodes;
            
        } catch (Exception e) {
            System.err.println("过滤节点失败: " + e.getMessage());
            e.printStackTrace();
            return nodes; // 如果出错，返回所有节点
        }
    }
    
    /**
     * 根据条件表达式过滤节点，只返回符合条件的路径上的节点（支持CtrlPayout）
     */
    private List<ProcessNodeInfo> filterNodesByConditions(List<ProcessNodeInfo> nodes, String processXml, CtrlPayout ctrlPayout) {
        System.out.println("=== filterNodesByConditions (CtrlPayout) 开始 ===");
        System.out.println("原始节点数量: " + nodes.size());
        
        try {
            // 解析BPMN XML，构建节点连接关系
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new org.xml.sax.InputSource(new StringReader(processXml)));
            
            // 构建节点ID到节点的映射
            Map<String, ProcessNodeInfo> nodeMap = new HashMap<>();
            for (ProcessNodeInfo node : nodes) {
                nodeMap.put(node.getId(), node);
            }
            
            // 构建连接关系：sourceRef -> List<{targetRef, conditionExpression}>
            Map<String, List<FlowInfo>> flowMap = new HashMap<>();
            
            // 找到startEvent
            String startEventId = null;
            NodeList allElements = doc.getElementsByTagName("*");
            for (int i = 0; i < allElements.getLength(); i++) {
                Element element = (Element) allElements.item(i);
                String localName = element.getLocalName();
                if (localName == null) {
                    String tagName = element.getTagName();
                    int colonIndex = tagName.indexOf(':');
                    localName = colonIndex >= 0 ? tagName.substring(colonIndex + 1) : tagName;
                }
                
                if ("startEvent".equals(localName)) {
                    startEventId = element.getAttribute("id");
                    System.out.println("找到startEvent: " + startEventId);
                    break;
                }
            }
            
            // 解析所有sequenceFlow
            for (int i = 0; i < allElements.getLength(); i++) {
                Element element = (Element) allElements.item(i);
                String localName = element.getLocalName();
                if (localName == null) {
                    String tagName = element.getTagName();
                    int colonIndex = tagName.indexOf(':');
                    localName = colonIndex >= 0 ? tagName.substring(colonIndex + 1) : tagName;
                }
                
                if ("sequenceFlow".equals(localName)) {
                    String sourceRef = element.getAttribute("sourceRef");
                    String targetRef = element.getAttribute("targetRef");
                    String conditionExpression = null;
                    
                    // 提取条件表达式
                    NodeList childNodes = element.getChildNodes();
                    for (int j = 0; j < childNodes.getLength(); j++) {
                        if (childNodes.item(j) instanceof Element) {
                            Element childElement = (Element) childNodes.item(j);
                            String childLocalName = childElement.getLocalName();
                            if (childLocalName == null) {
                                String childTagName = childElement.getTagName();
                                int colonIndex = childTagName.indexOf(':');
                                childLocalName = colonIndex >= 0 ? childTagName.substring(colonIndex + 1) : childTagName;
                            }
                            if ("conditionExpression".equals(childLocalName)) {
                                conditionExpression = childElement.getTextContent();
                                break;
                            }
                        }
                    }
                    
                    if (sourceRef != null && targetRef != null) {
                        flowMap.computeIfAbsent(sourceRef, k -> new ArrayList<>()).add(new FlowInfo(targetRef, conditionExpression));
                        System.out.println("找到连线: " + sourceRef + " -> " + targetRef + (conditionExpression != null ? " (条件: " + conditionExpression + ")" : ""));
                    }
                }
            }
            
            if (startEventId == null) {
                System.out.println("未找到startEvent，返回所有节点");
                return nodes;
            }
            
            // 从业务对象中提取变量值
            Map<String, Object> variables = extractVariablesFromCtrlPayout(ctrlPayout);
            System.out.println("提取的业务变量: " + variables);
            
            // 从startEvent开始，沿着满足条件的路径遍历
            Set<String> visitedNodes = new HashSet<>();
            Set<String> reachableNodes = new HashSet<>();
            traverseFromNode(startEventId, flowMap, variables, visitedNodes, reachableNodes);
            
            System.out.println("可达节点: " + reachableNodes);
            
            // 过滤节点，只保留可达的节点
            List<ProcessNodeInfo> filteredNodes = new ArrayList<>();
            for (ProcessNodeInfo node : nodes) {
                if (reachableNodes.contains(node.getId())) {
                    filteredNodes.add(node);
                }
            }
            
            // 按照流程执行顺序排序节点（拓扑排序）
            filteredNodes = sortNodesByFlowOrder(filteredNodes, flowMap, startEventId, variables);
            
            System.out.println("过滤后节点数量: " + filteredNodes.size());
            System.out.println("=== filterNodesByConditions (CtrlPayout) 完成 ===");
            return filteredNodes;
            
        } catch (Exception e) {
            System.err.println("过滤节点失败: " + e.getMessage());
            e.printStackTrace();
            return nodes; // 如果出错，返回所有节点
        }
    }
    
    /**
     * 根据条件表达式过滤节点，只返回符合条件的路径上的节点（支持PactMain）
     */
    private List<ProcessNodeInfo> filterNodesByConditions(List<ProcessNodeInfo> nodes, String processXml, PactMain pactMain) {
        System.out.println("=== filterNodesByConditions (PactMain) 开始 ===");
        System.out.println("原始节点数量: " + nodes.size());
        
        try {
            // 解析BPMN XML，构建节点连接关系
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new org.xml.sax.InputSource(new StringReader(processXml)));
            
            // 构建节点ID到节点的映射
            Map<String, ProcessNodeInfo> nodeMap = new HashMap<>();
            for (ProcessNodeInfo node : nodes) {
                nodeMap.put(node.getId(), node);
            }
            
            // 构建连接关系：sourceRef -> List<{targetRef, conditionExpression}>
            Map<String, List<FlowInfo>> flowMap = new HashMap<>();
            
            // 找到startEvent
            String startEventId = null;
            NodeList allElements = doc.getElementsByTagName("*");
            for (int i = 0; i < allElements.getLength(); i++) {
                Element element = (Element) allElements.item(i);
                String localName = element.getLocalName();
                if (localName == null) {
                    String tagName = element.getTagName();
                    int colonIndex = tagName.indexOf(':');
                    localName = colonIndex >= 0 ? tagName.substring(colonIndex + 1) : tagName;
                }
                
                if ("startEvent".equals(localName)) {
                    startEventId = element.getAttribute("id");
                    System.out.println("找到startEvent: " + startEventId);
                    break;
                }
            }
            
            // 解析所有sequenceFlow
            for (int i = 0; i < allElements.getLength(); i++) {
                Element element = (Element) allElements.item(i);
                String localName = element.getLocalName();
                if (localName == null) {
                    String tagName = element.getTagName();
                    int colonIndex = tagName.indexOf(':');
                    localName = colonIndex >= 0 ? tagName.substring(colonIndex + 1) : tagName;
                }
                
                if ("sequenceFlow".equals(localName)) {
                    String sourceRef = element.getAttribute("sourceRef");
                    String targetRef = element.getAttribute("targetRef");
                    String conditionExpression = null;
                    
                    // 提取条件表达式
                    NodeList childNodes = element.getChildNodes();
                    for (int j = 0; j < childNodes.getLength(); j++) {
                        if (childNodes.item(j) instanceof Element) {
                            Element childElement = (Element) childNodes.item(j);
                            String childLocalName = childElement.getLocalName();
                            if (childLocalName == null) {
                                String childTagName = childElement.getTagName();
                                int colonIndex = childTagName.indexOf(':');
                                childLocalName = colonIndex >= 0 ? childTagName.substring(colonIndex + 1) : childTagName;
                            }
                            if ("conditionExpression".equals(childLocalName)) {
                                conditionExpression = childElement.getTextContent();
                                break;
                            }
                        }
                    }
                    
                    if (sourceRef != null && targetRef != null) {
                        flowMap.computeIfAbsent(sourceRef, k -> new ArrayList<>()).add(new FlowInfo(targetRef, conditionExpression));
                        System.out.println("找到连线: " + sourceRef + " -> " + targetRef + (conditionExpression != null ? " (条件: " + conditionExpression + ")" : ""));
                    }
                }
            }
            
            if (startEventId == null) {
                System.out.println("未找到startEvent，返回所有节点");
                return nodes;
            }
            
            // 从业务对象中提取变量值
            Map<String, Object> variables = extractVariablesFromPactMain(pactMain);
            System.out.println("提取的业务变量: " + variables);
            
            // 从startEvent开始，沿着满足条件的路径遍历
            Set<String> visitedNodes = new HashSet<>();
            Set<String> reachableNodes = new HashSet<>();
            traverseFromNode(startEventId, flowMap, variables, visitedNodes, reachableNodes);
            
            System.out.println("可达节点: " + reachableNodes);
            
            // 过滤节点，只保留可达的节点
            List<ProcessNodeInfo> filteredNodes = new ArrayList<>();
            for (ProcessNodeInfo node : nodes) {
                if (reachableNodes.contains(node.getId())) {
                    filteredNodes.add(node);
                }
            }
            
            // 按照流程执行顺序排序节点（拓扑排序）
            filteredNodes = sortNodesByFlowOrder(filteredNodes, flowMap, startEventId, variables);
            
            System.out.println("过滤后节点数量: " + filteredNodes.size());
            System.out.println("=== filterNodesByConditions (PactMain) 完成 ===");
            return filteredNodes;
            
        } catch (Exception e) {
            System.err.println("过滤节点失败: " + e.getMessage());
            e.printStackTrace();
            return nodes; // 如果出错，返回所有节点
        }
    }
    
    /**
     * 根据条件表达式过滤节点，只返回符合条件的路径上的节点（支持HrApply）
     */
    private List<ProcessNodeInfo> filterNodesByConditions(List<ProcessNodeInfo> nodes, String processXml, com.hrp.common.entity.HrApply hrApply) {
        System.out.println("=== filterNodesByConditions (HrApply) 开始 ===");
        System.out.println("原始节点数量: " + nodes.size());
        
        try {
            // 解析BPMN XML，构建节点连接关系
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new org.xml.sax.InputSource(new StringReader(processXml)));
            
            // 构建节点ID到节点的映射
            Map<String, ProcessNodeInfo> nodeMap = new HashMap<>();
            for (ProcessNodeInfo node : nodes) {
                nodeMap.put(node.getId(), node);
            }
            
            // 构建连接关系：sourceRef -> List<{targetRef, conditionExpression}>
            Map<String, List<FlowInfo>> flowMap = new HashMap<>();
            
            // 找到startEvent
            String startEventId = null;
            NodeList allElements = doc.getElementsByTagName("*");
            for (int i = 0; i < allElements.getLength(); i++) {
                Element element = (Element) allElements.item(i);
                String localName = element.getLocalName();
                if (localName == null) {
                    String tagName = element.getTagName();
                    int colonIndex = tagName.indexOf(':');
                    localName = colonIndex >= 0 ? tagName.substring(colonIndex + 1) : tagName;
                }
                
                if ("startEvent".equals(localName)) {
                    startEventId = element.getAttribute("id");
                    System.out.println("找到startEvent: " + startEventId);
                    break;
                }
            }
            
            // 解析所有sequenceFlow
            for (int i = 0; i < allElements.getLength(); i++) {
                Element element = (Element) allElements.item(i);
                String localName = element.getLocalName();
                if (localName == null) {
                    String tagName = element.getTagName();
                    int colonIndex = tagName.indexOf(':');
                    localName = colonIndex >= 0 ? tagName.substring(colonIndex + 1) : tagName;
                }
                
                if ("sequenceFlow".equals(localName)) {
                    String sourceRef = element.getAttribute("sourceRef");
                    String targetRef = element.getAttribute("targetRef");
                    String conditionExpression = null;
                    
                    // 提取条件表达式
                    NodeList childNodes = element.getChildNodes();
                    for (int j = 0; j < childNodes.getLength(); j++) {
                        if (childNodes.item(j) instanceof Element) {
                            Element childElement = (Element) childNodes.item(j);
                            String childLocalName = childElement.getLocalName();
                            if (childLocalName == null) {
                                String childTagName = childElement.getTagName();
                                int colonIndex = childTagName.indexOf(':');
                                childLocalName = colonIndex >= 0 ? childTagName.substring(colonIndex + 1) : childTagName;
                            }
                            if ("conditionExpression".equals(childLocalName)) {
                                conditionExpression = childElement.getTextContent();
                                break;
                            }
                        }
                    }
                    
                    if (sourceRef != null && targetRef != null) {
                        flowMap.computeIfAbsent(sourceRef, k -> new ArrayList<>()).add(new FlowInfo(targetRef, conditionExpression));
                        System.out.println("找到连线: " + sourceRef + " -> " + targetRef + (conditionExpression != null ? " (条件: " + conditionExpression + ")" : ""));
                    }
                }
            }
            
            if (startEventId == null) {
                System.out.println("未找到startEvent，返回所有节点");
                return nodes;
            }
            
            // 从业务对象中提取变量值
            Map<String, Object> variables = extractVariablesFromHrApply(hrApply);
            System.out.println("提取的业务变量: " + variables);
            
            // 从startEvent开始，沿着满足条件的路径遍历
            Set<String> visitedNodes = new HashSet<>();
            Set<String> reachableNodes = new HashSet<>();
            traverseFromNode(startEventId, flowMap, variables, visitedNodes, reachableNodes);
            
            System.out.println("可达节点: " + reachableNodes);
            
            // 过滤节点，只保留可达的节点
            List<ProcessNodeInfo> filteredNodes = new ArrayList<>();
            for (ProcessNodeInfo node : nodes) {
                if (reachableNodes.contains(node.getId())) {
                    filteredNodes.add(node);
                }
            }
            
            // 按照流程执行顺序排序节点（拓扑排序）
            filteredNodes = sortNodesByFlowOrder(filteredNodes, flowMap, startEventId, variables);
            
            System.out.println("过滤后节点数量: " + filteredNodes.size());
            System.out.println("=== filterNodesByConditions (HrApply) 完成 ===");
            return filteredNodes;
            
        } catch (Exception e) {
            System.err.println("过滤节点失败: " + e.getMessage());
            e.printStackTrace();
            return nodes; // 如果出错，返回所有节点
        }
    }
    
    /**
     * 从业务对象中提取变量值
     */
    private Map<String, Object> extractVariablesFromBudgetApply(BudgetApply budgetApply) {
        Map<String, Object> variables = new HashMap<>();
        if (budgetApply == null) {
            return variables;
        }
        
        try {
            java.lang.reflect.Field[] fields = budgetApply.getClass().getDeclaredFields();
            for (java.lang.reflect.Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(budgetApply);
                // 即使值为null也要放入，因为可能需要处理null值
                variables.put(field.getName(), value);
                
                // 同时也支持下划线命名（apply_amount -> applyAmount）
                String underscoreName = convertToUnderscore(field.getName());
                if (!underscoreName.equals(field.getName())) {
                    variables.put(underscoreName, value);
                }
            }
            System.out.println("提取的变量: " + variables.keySet());
        } catch (Exception e) {
            System.err.println("提取变量失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return variables;
    }
    
    /**
     * 从CtrlPayout业务对象中提取变量值
     */
    private Map<String, Object> extractVariablesFromCtrlPayout(CtrlPayout ctrlPayout) {
        Map<String, Object> variables = new HashMap<>();
        if (ctrlPayout == null) {
            return variables;
        }
        
        try {
            java.lang.reflect.Field[] fields = ctrlPayout.getClass().getDeclaredFields();
            for (java.lang.reflect.Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(ctrlPayout);
                // 即使值为null也要放入，因为可能需要处理null值
                variables.put(field.getName(), value);
                
                // 同时也支持下划线命名（apply_amount -> applyAmount）
                String underscoreName = convertToUnderscore(field.getName());
                if (!underscoreName.equals(field.getName())) {
                    variables.put(underscoreName, value);
                }
            }
            System.out.println("提取的变量: " + variables.keySet());
        } catch (Exception e) {
            System.err.println("提取变量失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return variables;
    }
    
    /**
     * 从HrApply业务对象中提取变量值
     */
    private Map<String, Object> extractVariablesFromHrApply(com.hrp.common.entity.HrApply hrApply) {
        Map<String, Object> variables = new HashMap<>();
        if (hrApply == null) {
            return variables;
        }
        
        try {
            java.lang.reflect.Field[] fields = hrApply.getClass().getDeclaredFields();
            for (java.lang.reflect.Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(hrApply);
                // 即使值为null也要放入，因为可能需要处理null值
                variables.put(field.getName(), value);
                
                // 同时也支持下划线命名（is_nurse -> isNurse）
                String underscoreName = convertToUnderscore(field.getName());
                if (!underscoreName.equals(field.getName())) {
                    variables.put(underscoreName, value);
                }
            }
            System.out.println("提取的变量: " + variables.keySet());
        } catch (Exception e) {
            System.err.println("提取变量失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return variables;
    }
    
    /**
     * 从PactMain业务对象中提取变量值
     */
    private Map<String, Object> extractVariablesFromPactMain(PactMain pactMain) {
        Map<String, Object> variables = new HashMap<>();
        if (pactMain == null) {
            return variables;
        }
        
        try {
            java.lang.reflect.Field[] fields = pactMain.getClass().getDeclaredFields();
            for (java.lang.reflect.Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(pactMain);
                // 即使值为null也要放入，因为可能需要处理null值
                variables.put(field.getName(), value);
                
                // 同时也支持下划线命名（contract_amount -> contractAmount）
                String underscoreName = convertToUnderscore(field.getName());
                if (!underscoreName.equals(field.getName())) {
                    variables.put(underscoreName, value);
                }
            }
            System.out.println("提取的变量: " + variables.keySet());
        } catch (Exception e) {
            System.err.println("提取变量失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return variables;
    }
    
    /**
     * 根据条件表达式过滤节点，只返回符合条件的路径上的节点（支持AssetPurchaseApplyMain）
     */
    private List<ProcessNodeInfo> filterNodesByConditions(List<ProcessNodeInfo> nodes, String processXml, AssetPurchaseApplyMain assetPurchaseApply) {
        System.out.println("=== filterNodesByConditions (AssetPurchaseApplyMain) 开始 ===");
        System.out.println("原始节点数量: " + nodes.size());
        
        try {
            // 解析BPMN XML，构建节点连接关系
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new org.xml.sax.InputSource(new StringReader(processXml)));
            
            // 构建节点ID到节点的映射
            Map<String, ProcessNodeInfo> nodeMap = new HashMap<>();
            for (ProcessNodeInfo node : nodes) {
                nodeMap.put(node.getId(), node);
            }
            
            // 构建连接关系：sourceRef -> List<{targetRef, conditionExpression}>
            Map<String, List<FlowInfo>> flowMap = new HashMap<>();
            
            // 找到startEvent
            String startEventId = null;
            NodeList allElements = doc.getElementsByTagName("*");
            for (int i = 0; i < allElements.getLength(); i++) {
                Element element = (Element) allElements.item(i);
                String localName = element.getLocalName();
                if (localName == null) {
                    String tagName = element.getTagName();
                    int colonIndex = tagName.indexOf(':');
                    localName = colonIndex >= 0 ? tagName.substring(colonIndex + 1) : tagName;
                }
                
                if ("startEvent".equals(localName)) {
                    startEventId = element.getAttribute("id");
                    System.out.println("找到startEvent: " + startEventId);
                    break;
                }
            }
            
            // 解析所有sequenceFlow
            for (int i = 0; i < allElements.getLength(); i++) {
                Element element = (Element) allElements.item(i);
                String localName = element.getLocalName();
                if (localName == null) {
                    String tagName = element.getTagName();
                    int colonIndex = tagName.indexOf(':');
                    localName = colonIndex >= 0 ? tagName.substring(colonIndex + 1) : tagName;
                }
                
                if ("sequenceFlow".equals(localName)) {
                    String sourceRef = element.getAttribute("sourceRef");
                    String targetRef = element.getAttribute("targetRef");
                    String conditionExpression = null;
                    
                    // 提取条件表达式
                    NodeList childNodes = element.getChildNodes();
                    for (int j = 0; j < childNodes.getLength(); j++) {
                        if (childNodes.item(j) instanceof Element) {
                            Element childElement = (Element) childNodes.item(j);
                            String childLocalName = childElement.getLocalName();
                            if (childLocalName == null) {
                                String childTagName = childElement.getTagName();
                                int colonIndex = childTagName.indexOf(':');
                                childLocalName = colonIndex >= 0 ? childTagName.substring(colonIndex + 1) : childTagName;
                            }
                            if ("conditionExpression".equals(childLocalName)) {
                                conditionExpression = childElement.getTextContent();
                                break;
                            }
                        }
                    }
                    
                    if (sourceRef != null && targetRef != null) {
                        flowMap.computeIfAbsent(sourceRef, k -> new ArrayList<>()).add(new FlowInfo(targetRef, conditionExpression));
                        System.out.println("找到连线: " + sourceRef + " -> " + targetRef + (conditionExpression != null ? " (条件: " + conditionExpression + ")" : ""));
                    }
                }
            }
            
            if (startEventId == null) {
                System.out.println("未找到startEvent，返回所有节点");
                return nodes;
            }
            
            // 从业务对象中提取变量值
            Map<String, Object> variables = extractVariablesFromAssetPurchaseApply(assetPurchaseApply);
            System.out.println("提取的业务变量: " + variables);
            
            // 从startEvent开始，沿着满足条件的路径遍历
            Set<String> visitedNodes = new HashSet<>();
            Set<String> reachableNodes = new HashSet<>();
            traverseFromNode(startEventId, flowMap, variables, visitedNodes, reachableNodes);
            
            System.out.println("可达节点: " + reachableNodes);
            
            // 过滤节点，只保留可达的节点
            List<ProcessNodeInfo> filteredNodes = new ArrayList<>();
            for (ProcessNodeInfo node : nodes) {
                if (reachableNodes.contains(node.getId())) {
                    filteredNodes.add(node);
                }
            }
            
            // 按照流程执行顺序排序节点（拓扑排序）
            filteredNodes = sortNodesByFlowOrder(filteredNodes, flowMap, startEventId, variables);
            
            System.out.println("过滤后节点数量: " + filteredNodes.size());
            System.out.println("=== filterNodesByConditions (AssetPurchaseApplyMain) 完成 ===");
            return filteredNodes;
            
        } catch (Exception e) {
            System.err.println("过滤节点失败: " + e.getMessage());
            e.printStackTrace();
            return nodes; // 如果出错，返回所有节点
        }
    }
    
    /**
     * 从AssetPurchaseApplyMain业务对象中提取变量值
     */
    private Map<String, Object> extractVariablesFromAssetPurchaseApply(AssetPurchaseApplyMain assetPurchaseApply) {
        Map<String, Object> variables = new HashMap<>();
        if (assetPurchaseApply == null) {
            return variables;
        }
        
        try {
            java.lang.reflect.Field[] fields = assetPurchaseApply.getClass().getDeclaredFields();
            for (java.lang.reflect.Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(assetPurchaseApply);
                // 即使值为null也要放入，因为可能需要处理null值
                variables.put(field.getName(), value);
                
                // 同时也支持下划线命名（apply_money -> applyMoney）
                String underscoreName = convertToUnderscore(field.getName());
                if (!underscoreName.equals(field.getName())) {
                    variables.put(underscoreName, value);
                }
            }
            System.out.println("提取的变量: " + variables.keySet());
        } catch (Exception e) {
            System.err.println("提取变量失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return variables;
    }
    
    /**
     * 将驼峰命名转换为下划线命名（applyAmount -> apply_amount）
     */
    private String convertToUnderscore(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    result.append('_');
                }
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
    
    /**
     * 递归遍历节点，标记可达的节点
     */
    private void traverseFromNode(String nodeId, Map<String, List<FlowInfo>> flowMap, Map<String, Object> variables,
                                  Set<String> visitedNodes, Set<String> reachableNodes) {
        if (visitedNodes.contains(nodeId)) {
            return;
        }
        
        visitedNodes.add(nodeId);
        reachableNodes.add(nodeId);
        
        List<FlowInfo> flows = flowMap.get(nodeId);
        if (flows != null) {
            for (FlowInfo flow : flows) {
                // 如果有条件表达式，评估它
                if (flow.conditionExpression != null && !flow.conditionExpression.trim().isEmpty()) {
                    boolean conditionMet = evaluateCondition(flow.conditionExpression, variables);
                    System.out.println("评估条件: " + flow.conditionExpression + " = " + conditionMet);
                    if (conditionMet) {
                        traverseFromNode(flow.targetRef, flowMap, variables, visitedNodes, reachableNodes);
                    }
                } else {
                    // 没有条件表达式，直接遍历
                    traverseFromNode(flow.targetRef, flowMap, variables, visitedNodes, reachableNodes);
                }
            }
        }
    }
    
    /**
     * 评估条件表达式
     * 支持简单的表达式，如: ${applyAmount > 50000} 或 ${budget_apply.apply_amount<50000}
     * 支持多个条件组合，如: ${budget_apply.apply_amount<50000 && budget_apply.deptId==1111}
     */
    private boolean evaluateCondition(String conditionExpression, Map<String, Object> variables) {
        if (conditionExpression == null || conditionExpression.trim().isEmpty()) {
            return true; // 无条件，默认通过
        }
        
        try {
            String expression = conditionExpression.trim();
            
            // 处理 ${...} 格式
            java.util.regex.Pattern pattern1 = java.util.regex.Pattern.compile("\\$\\{([^}]+)\\}");
            java.util.regex.Matcher matcher1 = pattern1.matcher(expression);
            
            if (matcher1.find()) {
                String innerExpr = matcher1.group(1);
                System.out.println("提取内部表达式: " + innerExpr);
                
                // 先替换所有变量为实际值
                String evaluatedExpr = replaceVariables(innerExpr, variables);
                System.out.println("替换变量后表达式: " + evaluatedExpr);
                
                // 评估表达式（支持多个条件组合）
                return evaluateComplexExpression(evaluatedExpr);
            }
            
            // 如果没有${}，先替换变量再评估
            String evaluatedExpr = replaceVariables(expression, variables);
            System.out.println("评估表达式（无${}）: " + evaluatedExpr);
            return evaluateComplexExpression(evaluatedExpr);
            
        } catch (Exception e) {
            System.err.println("评估条件表达式失败: " + conditionExpression + ", 错误: " + e.getMessage());
            e.printStackTrace();
            return false; // 如果评估失败，默认不通过
        }
    }
    
    /**
     * 替换表达式中的变量为实际值
     * 支持 budget_apply.apply_amount 格式，转换为 applyAmount
     */
    private String replaceVariables(String expression, Map<String, Object> variables) {
        // 使用正则表达式匹配变量名（可能是 budget_apply.apply_amount 格式）
        // 匹配模式：可能的对象前缀.变量名 或 直接的变量名
        java.util.regex.Pattern varPattern = java.util.regex.Pattern.compile("([a-zA-Z_][a-zA-Z0-9_]*\\.)?([a-zA-Z_][a-zA-Z0-9_]*)");
        java.util.regex.Matcher matcher = varPattern.matcher(expression);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String fullMatch = matcher.group(0);
            String prefix = matcher.group(1); // 如 "budget_apply."
            String varName = matcher.group(2); // 如 "apply_amount"
            
            // 跳过操作符、数字、逻辑运算符
            if (fullMatch.matches("[><=!&|]+|\\d+(\\.\\d+)?")) {
                matcher.appendReplacement(result, fullMatch);
                continue;
            }
            
            // 转换为驼峰命名
            String camelName = convertToCamelCase(varName);
            Object value = variables.get(camelName);
            
            if (value != null) {
                // 找到变量值，替换
                matcher.appendReplacement(result, formatValue(value));
                System.out.println("替换变量: " + fullMatch + " -> " + formatValue(value) + " (变量名: " + camelName + ")");
            } else {
                // 没找到，尝试用原始名称查找（支持下划线格式）
                value = variables.get(varName);
                if (value != null) {
                    matcher.appendReplacement(result, formatValue(value));
                    System.out.println("替换变量（原始名称）: " + fullMatch + " -> " + formatValue(value));
                } else {
                    // 还是没找到，保持原样（可能是常量或其他）
                    matcher.appendReplacement(result, fullMatch);
                    System.out.println("变量未找到，保持原样: " + fullMatch + " (尝试的变量名: " + camelName + ")");
                }
            }
        }
        matcher.appendTail(result);
        
        return result.toString();
    }
    
    /**
     * 评估复杂表达式（支持多个条件组合，使用 && 和 ||）
     */
    private boolean evaluateComplexExpression(String expression) {
        expression = expression.trim();
        if (expression.isEmpty()) {
            return true;
        }
        
        try {
            // 去掉外层括号（如果整个表达式被括号包围）
            while (expression.startsWith("(") && expression.endsWith(")")) {
                // 检查括号是否匹配（找到最外层的闭合括号）
                int depth = 0;
                boolean isOuterParentheses = true;
                for (int i = 0; i < expression.length(); i++) {
                    char c = expression.charAt(i);
                    if (c == '(') {
                        depth++;
                    } else if (c == ')') {
                        depth--;
                        if (depth == 0 && i < expression.length() - 1) {
                            // 在最后一个字符之前就闭合了，说明不是最外层括号
                            isOuterParentheses = false;
                            break;
                        }
                    }
                }
                if (isOuterParentheses) {
                    expression = expression.substring(1, expression.length() - 1).trim();
                } else {
                    break;
                }
            }
            
            // 先处理 || (优先级最低)
            if (expression.contains("||")) {
                String[] parts = splitByOperator(expression, "||");
                for (String part : parts) {
                    if (evaluateComplexExpression(part.trim())) {
                        return true; // 任何一个为true就返回true
                    }
                }
                return false; // 所有都为false
            }
            
            // 处理 && (优先级较高)
            if (expression.contains("&&")) {
                String[] parts = splitByOperator(expression, "&&");
                for (String part : parts) {
                    if (!evaluateComplexExpression(part.trim())) {
                        return false; // 任何一个为false就返回false
                    }
                }
                return true; // 所有都为true
            }
            
            // 没有逻辑运算符，评估单个表达式
            return evaluateSimpleExpression(expression);
            
        } catch (Exception e) {
            System.err.println("评估复杂表达式失败: " + expression + ", 错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 按运算符分割表达式（考虑括号的嵌套）
     */
    private String[] splitByOperator(String expression, String operator) {
        java.util.List<String> parts = new java.util.ArrayList<>();
        int depth = 0;
        int start = 0;
        
        for (int i = 0; i <= expression.length() - operator.length(); i++) {
            char c = expression.charAt(i);
            if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
            } else if (depth == 0 && expression.substring(i).startsWith(operator)) {
                parts.add(expression.substring(start, i).trim());
                start = i + operator.length();
                i += operator.length() - 1;
            }
        }
        
        // 添加最后一部分
        if (start < expression.length()) {
            parts.add(expression.substring(start).trim());
        }
        
        return parts.toArray(new String[0]);
    }
    
    /**
     * 将下划线命名转换为驼峰命名（apply_amount -> applyAmount）
     */
    private String convertToCamelCase(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        String[] parts = name.split("_");
        StringBuilder result = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            if (parts[i].length() > 0) {
                result.append(parts[i].substring(0, 1).toUpperCase());
                if (parts[i].length() > 1) {
                    result.append(parts[i].substring(1));
                }
            }
        }
        return result.toString();
    }
    
    /**
     * 格式化值为字符串
     */
    private String formatValue(Object value) {
        if (value == null) {
            return "0";
        }
        if (value instanceof Number) {
            return value.toString();
        }
        if (value instanceof String) {
            return "\"" + value.toString() + "\"";
        }
        return value.toString();
    }
    
    /**
     * 评估简单表达式（如: applyAmount > 50000 或 status == "PENDING"）
     * 支持基本的比较操作: >, <, >=, <=, ==, !=, =
     * 支持数字和字符串比较
     */
    private boolean evaluateSimpleExpression(String expression) {
        try {
            expression = expression.trim();
            
            // 支持常见的比较操作符（注意顺序：先匹配长的操作符）
            String[] operators = {">=", "<=", "==", "!=", ">", "<", "="};
            for (String op : operators) {
                // 需要找到操作符的位置，但要避免在引号内
                int index = findOperatorOutsideQuotes(expression, op);
                if (index > 0) {
                    String left = expression.substring(0, index).trim();
                    String right = expression.substring(index + op.length()).trim();
                    
                    // 解析左右两边的值（处理字符串引号）
                    Object leftValue = parseValue(left);
                    Object rightValue = parseValue(right);
                    
                    System.out.println("比较: " + leftValue + " (" + (leftValue != null ? leftValue.getClass().getSimpleName() : "null") + 
                                     ") " + op + " " + rightValue + " (" + (rightValue != null ? rightValue.getClass().getSimpleName() : "null") + ")");
                    
                    // 字符串比较
                    if (leftValue instanceof String || rightValue instanceof String) {
                        String leftStr = leftValue != null ? leftValue.toString() : "";
                        String rightStr = rightValue != null ? rightValue.toString() : "";
                        
                        int compareResult = leftStr.compareTo(rightStr);
                        
                        switch (op) {
                            case "==":
                            case "=":
                                return leftStr.equals(rightStr);
                            case "!=":
                                return !leftStr.equals(rightStr);
                            case ">":
                                return compareResult > 0;
                            case "<":
                                return compareResult < 0;
                            case ">=":
                                return compareResult >= 0;
                            case "<=":
                                return compareResult <= 0;
                        }
                    }
                    // 数字比较
                    else if (leftValue instanceof Number || rightValue instanceof Number) {
                        double leftNum = toDouble(leftValue);
                        double rightNum = toDouble(rightValue);
                        
                        switch (op) {
                            case ">":
                                return leftNum > rightNum;
                            case "<":
                                return leftNum < rightNum;
                            case ">=":
                                return leftNum >= rightNum;
                            case "<=":
                                return leftNum <= rightNum;
                            case "==":
                            case "=":
                                return Math.abs(leftNum - rightNum) < 0.0001; // 浮点数比较
                            case "!=":
                                return Math.abs(leftNum - rightNum) >= 0.0001;
                        }
                    }
                    // 其他类型（如Boolean）
                    else {
                        // 尝试转换为字符串比较
                        String leftStr = leftValue != null ? leftValue.toString() : "";
                        String rightStr = rightValue != null ? rightValue.toString() : "";
                        
                        switch (op) {
                            case "==":
                            case "=":
                                return leftStr.equals(rightStr);
                            case "!=":
                                return !leftStr.equals(rightStr);
                            default:
                                // 对于其他操作符，尝试转换为数字
                                double leftNum = toDouble(leftValue);
                                double rightNum = toDouble(rightValue);
                                switch (op) {
                                    case ">":
                                        return leftNum > rightNum;
                                    case "<":
                                        return leftNum < rightNum;
                                    case ">=":
                                        return leftNum >= rightNum;
                                    case "<=":
                                        return leftNum <= rightNum;
                                }
                        }
                    }
                }
            }
            
            // 如果没有找到操作符，尝试作为布尔值处理
            if ("true".equalsIgnoreCase(expression)) {
                return true;
            } else if ("false".equalsIgnoreCase(expression)) {
                return false;
            }
            
            System.err.println("无法解析表达式: " + expression);
            return false;
            
        } catch (Exception e) {
            System.err.println("评估表达式失败: " + expression + ", 错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 在表达式中查找操作符，但忽略引号内的内容
     */
    private int findOperatorOutsideQuotes(String expression, String operator) {
        boolean inSingleQuotes = false;
        boolean inDoubleQuotes = false;
        
        for (int i = 0; i <= expression.length() - operator.length(); i++) {
            char c = expression.charAt(i);
            
            // 处理引号
            if (c == '"' && (i == 0 || expression.charAt(i - 1) != '\\')) {
                inDoubleQuotes = !inDoubleQuotes;
            } else if (c == '\'' && (i == 0 || expression.charAt(i - 1) != '\\')) {
                inSingleQuotes = !inSingleQuotes;
            }
            
            // 如果不在引号内，检查是否匹配操作符
            if (!inSingleQuotes && !inDoubleQuotes) {
                if (expression.substring(i).startsWith(operator)) {
                    return i;
                }
            }
        }
        
        return -1;
    }
    
    /**
     * 解析值（支持数字、字符串、变量名）
     */
    private Object parseValue(String value) {
        value = value.trim();
        
        // 移除引号
        if ((value.startsWith("\"") && value.endsWith("\"")) || 
            (value.startsWith("'") && value.endsWith("'"))) {
            return value.substring(1, value.length() - 1);
        }
        
        // 尝试解析为数字
        try {
            if (value.contains(".")) {
                return Double.parseDouble(value);
            } else {
                return Long.parseLong(value);
            }
        } catch (NumberFormatException e) {
            // 不是数字，返回原始字符串
            return value;
        }
    }
    
    /**
     * 转换为double
     */
    private double toDouble(Object value) {
        if (value == null) {
            return 0.0;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }
    
    /**
     * 连线信息
     */
    private static class FlowInfo {
        String targetRef;
        String conditionExpression;
        
        FlowInfo(String targetRef, String conditionExpression) {
            this.targetRef = targetRef;
            this.conditionExpression = conditionExpression;
        }
    }
    
    /**
     * 按照流程执行顺序排序节点（拓扑排序）
     * 从startEvent开始，按照sequenceFlow的顺序排序节点
     */
    private List<ProcessNodeInfo> sortNodesByFlowOrder(List<ProcessNodeInfo> nodes, Map<String, List<FlowInfo>> flowMap, 
                                                       String startEventId, Map<String, Object> variables) {
        if (nodes == null || nodes.isEmpty() || flowMap == null || startEventId == null) {
            return nodes;
        }
        
        // 构建节点ID到节点的映射
        Map<String, ProcessNodeInfo> nodeMap = new HashMap<>();
        for (ProcessNodeInfo node : nodes) {
            nodeMap.put(node.getId(), node);
        }
        
        // 使用拓扑排序，从startEvent开始，按照流程顺序收集节点
        List<ProcessNodeInfo> sortedNodes = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> processing = new HashSet<>();
        
        // 从startEvent开始遍历（后序遍历）
        traverseAndSort(startEventId, flowMap, nodeMap, variables, visited, processing, sortedNodes);
        
        // 由于是后序遍历，顺序是反的，需要反转列表得到正确的流程顺序
        java.util.Collections.reverse(sortedNodes);
        
        System.out.println("=== 按流程顺序排序节点（反转后） ===");
        for (int i = 0; i < sortedNodes.size(); i++) {
            System.out.println("节点 " + (i + 1) + ": " + sortedNodes.get(i).getName() + " (ID: " + sortedNodes.get(i).getId() + ")");
        }
        
        // 如果排序后的节点数量与原始节点数量不一致，可能有些节点没有连接到主流程，需要添加
        if (sortedNodes.size() < nodes.size()) {
            for (ProcessNodeInfo node : nodes) {
                if (!sortedNodes.contains(node)) {
                    sortedNodes.add(node);
                    System.out.println("添加未连接到主流程的节点: " + node.getName());
                }
            }
        }
        
        return sortedNodes;
    }
    
    /**
     * 遍历流程并排序节点（深度优先搜索，按执行顺序收集节点）
     * 使用后序遍历：先遍历所有后续节点，然后再添加当前节点
     * 但由于我们需要的是前序顺序（A -> B -> C），所以需要反转结果
     */
    private void traverseAndSort(String nodeId, Map<String, List<FlowInfo>> flowMap, Map<String, ProcessNodeInfo> nodeMap,
                                Map<String, Object> variables, Set<String> visited, Set<String> processing, 
                                List<ProcessNodeInfo> sortedNodes) {
        // 防止循环
        if (processing.contains(nodeId)) {
            return;
        }
        if (visited.contains(nodeId)) {
            return;
        }
        
        processing.add(nodeId);
        
        // 先遍历所有后续节点（深度优先）
        List<FlowInfo> flows = flowMap.get(nodeId);
        if (flows != null) {
            for (FlowInfo flow : flows) {
                // 如果有条件表达式，评估它（只遍历满足条件的路径）
                if (flow.conditionExpression != null && !flow.conditionExpression.trim().isEmpty()) {
                    boolean conditionMet = evaluateCondition(flow.conditionExpression, variables);
                    if (conditionMet) {
                        traverseAndSort(flow.targetRef, flowMap, nodeMap, variables, visited, processing, sortedNodes);
                    }
                } else {
                    // 没有条件表达式，直接遍历
                    traverseAndSort(flow.targetRef, flowMap, nodeMap, variables, visited, processing, sortedNodes);
                }
            }
        }
        
        // 遍历完后续节点后，如果是userTask节点，添加到排序列表中
        // 注意：由于是后序遍历，后续节点已经在列表中了，当前节点会添加在它们后面
        // 但我们需要的是前序顺序，所以需要在最后反转列表
        ProcessNodeInfo node = nodeMap.get(nodeId);
        if (node != null && "userTask".equals(node.getType())) {
            if (!sortedNodes.contains(node)) {
                sortedNodes.add(node);
            }
        }
        
        processing.remove(nodeId);
        visited.add(nodeId);
    }
}



