package com.hrp.auth.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrp.auth.mapper.ProcessDefinitionMapper;
import com.hrp.auth.service.ProcessDefinitionService;
import com.hrp.common.entity.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 流程定义服务实现类
 */
@Service
public class ProcessDefinitionServiceImpl implements ProcessDefinitionService {

    @Autowired
    private ProcessDefinitionMapper processDefinitionMapper;

    @Override
    public ProcessDefinition getById(Long definitionId) {
        return processDefinitionMapper.selectById(definitionId);
    }

    @Override
    public ProcessDefinition getByKey(String definitionKey) {
        return processDefinitionMapper.selectByKey(definitionKey);
    }

    @Override
    public List<ProcessDefinition> getByType(String definitionType) {
        return processDefinitionMapper.selectByType(definitionType);
    }

    @Override
    public List<ProcessDefinition> getAll() {
        return processDefinitionMapper.selectAll();
    }

    @Override
    public com.hrp.common.entity.PageResult<ProcessDefinition> getAllPage(Long page, Long size) {
        Long offset = (page - 1) * size;
        List<ProcessDefinition> records = processDefinitionMapper.selectAllPage(offset, size);
        Long total = processDefinitionMapper.countAll();
        return com.hrp.common.entity.PageResult.of(records, total, size, page);
    }

    @Override
    public com.hrp.common.entity.PageResult<ProcessDefinition> getByTypePage(String definitionType, Long page, Long size) {
        Long offset = (page - 1) * size;
        List<ProcessDefinition> records = processDefinitionMapper.selectByTypePage(definitionType, offset, size);
        Long total = processDefinitionMapper.countByType(definitionType);
        return com.hrp.common.entity.PageResult.of(records, total, size, page);
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
            // 构建导出JSON对象
            Map<String, Object> exportData = new HashMap<>();
            exportData.put("definitionKey", definition.getDefinitionKey());
            exportData.put("definitionName", definition.getDefinitionName());
            exportData.put("definitionType", definition.getDefinitionType());
            exportData.put("processXml", definition.getProcessXml());
            exportData.put("processJson", definition.getProcessJson());
            exportData.put("version", definition.getVersion());
            exportData.put("isActive", definition.getIsActive());
            exportData.put("description", definition.getDescription());
            exportData.put("exportTime", java.time.LocalDateTime.now().toString());
            exportData.put("version", "1.0");
            
            // 转换为JSON字符串
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(exportData);
        } catch (Exception e) {
            throw new RuntimeException("导出流程定义失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public ProcessDefinition importDefinition(String jsonContent, String definitionKey, String definitionName, String definitionType) {
        try {
            // 解析JSON内容
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonContent);
            
            // 创建新流程定义对象
            ProcessDefinition definition = new ProcessDefinition();
            
            // 从JSON中读取或使用传入的参数
            definition.setDefinitionKey(definitionKey != null ? definitionKey : 
                (jsonNode.has("definitionKey") ? jsonNode.get("definitionKey").asText() : null));
            definition.setDefinitionName(definitionName != null ? definitionName : 
                (jsonNode.has("definitionName") ? jsonNode.get("definitionName").asText() : null));
            definition.setDefinitionType(definitionType != null ? definitionType : 
                (jsonNode.has("definitionType") ? jsonNode.get("definitionType").asText() : "CUSTOM"));
            
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
            
            // 从JSON中读取流程内容
            if (jsonNode.has("processXml")) {
                definition.setProcessXml(jsonNode.get("processXml").asText());
            }
            if (jsonNode.has("processJson")) {
                definition.setProcessJson(jsonNode.get("processJson").asText());
            }
            if (jsonNode.has("version")) {
                definition.setVersion(jsonNode.get("version").asInt());
            } else {
                definition.setVersion(1);
            }
            if (jsonNode.has("description")) {
                definition.setDescription(jsonNode.get("description").asText());
            }
            
            // 设置默认值
            definition.setIsActive(1L);
            
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
}



