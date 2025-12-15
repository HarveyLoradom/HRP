package com.hrp.auth.service.impl;

import com.hrp.auth.mapper.ProcessInstanceMapper;
import com.hrp.auth.mapper.ProcessVariableMapper;
import com.hrp.auth.service.ProcessInstanceService;
import com.hrp.auth.service.ProcessDefinitionService;
import com.hrp.common.entity.ProcessInstance;
import com.hrp.common.entity.ProcessVariable;
import com.hrp.common.entity.ProcessDefinition;
import org.flowable.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 流程实例服务实现类
 */
@Service
public class ProcessInstanceServiceImpl implements ProcessInstanceService {

    @Autowired
    private ProcessInstanceMapper processInstanceMapper;

    @Autowired
    private ProcessVariableMapper processVariableMapper;

    @Autowired
    private ProcessDefinitionService processDefinitionService;

    @Autowired(required = false)
    private org.flowable.engine.ProcessEngine processEngine;
    
    private org.flowable.engine.RuntimeService getRuntimeService() {
        if (processEngine != null) {
            return processEngine.getRuntimeService();
        }
        return null;
    }

    @Override
    public ProcessInstance getById(Long instanceId) {
        return processInstanceMapper.selectById(instanceId);
    }

    @Override
    public ProcessInstance getByBusinessKey(String businessKey) {
        return processInstanceMapper.selectByBusinessKey(businessKey);
    }

    @Override
    public ProcessInstance getByBusiness(String businessType, Long businessId) {
        return processInstanceMapper.selectByBusiness(businessType, businessId);
    }

    @Override
    public List<ProcessInstance> getAll() {
        return processInstanceMapper.selectAll();
    }

    @Override
    public List<ProcessInstance> getByStatus(String processStatus) {
        return processInstanceMapper.selectByStatus(processStatus);
    }

    @Override
    public com.hrp.common.entity.PageResult<ProcessInstance> getAllPage(Long page, Long size) {
        Long offset = (page - 1) * size;
        List<ProcessInstance> records = processInstanceMapper.selectAllPage(offset, size);
        Long total = processInstanceMapper.countAll();
        return com.hrp.common.entity.PageResult.of(records, total, size, page);
    }

    @Override
    public com.hrp.common.entity.PageResult<ProcessInstance> getByStatusPage(String processStatus, Long page, Long size) {
        Long offset = (page - 1) * size;
        List<ProcessInstance> records = processInstanceMapper.selectByStatusPage(processStatus, offset, size);
        Long total = processInstanceMapper.countByStatus(processStatus);
        return com.hrp.common.entity.PageResult.of(records, total, size, page);
    }

    @Override
    public List<ProcessVariable> getVariablesByInstanceId(Long instanceId) {
        return processVariableMapper.selectByInstanceId(instanceId);
    }

    @Override
    @Transactional
    public boolean startProcess(ProcessInstance instance) {
        if (instance.getProcessStatus() == null) {
            instance.setProcessStatus("RUNNING");
        }
        return processInstanceMapper.insert(instance) > 0;
    }

    @Override
    @Transactional
    public ProcessInstance startProcess(Long processDefinitionId, String businessKey, String businessType, Long businessId) {
        try {
            // 获取流程定义
            ProcessDefinition definition = processDefinitionService.getById(processDefinitionId);
            if (definition == null || definition.getIsActive() != 1) {
                throw new RuntimeException("流程定义不存在或未启用");
            }

            // 如果Flowable可用，使用Flowable启动流程
            org.flowable.engine.RuntimeService runtimeService = getRuntimeService();
            if (runtimeService != null && definition.getProcessXml() != null && !definition.getProcessXml().isEmpty()) {
                return startProcessWithFlowable(definition, businessKey, businessType, businessId);
            } else {
                // 否则使用简单的方式创建流程实例
                return startProcessSimple(definition, businessKey, businessType, businessId);
            }
        } catch (Exception e) {
            throw new RuntimeException("启动流程失败：" + e.getMessage(), e);
        }
    }

    /**
     * 使用Flowable启动流程
     */
    private ProcessInstance startProcessWithFlowable(ProcessDefinition definition, String businessKey, String businessType, Long businessId) {
        try {
            // 部署流程定义（如果尚未部署）
            String deploymentId = deployProcessDefinition(definition);
            
            // 准备流程变量
            Map<String, Object> variables = new HashMap<>();
            variables.put("businessKey", businessKey);
            variables.put("businessType", businessType);
            variables.put("businessId", businessId);

            // 启动流程实例
            org.flowable.engine.RuntimeService runtimeService = getRuntimeService();
            if (runtimeService == null) {
                throw new RuntimeException("Flowable RuntimeService 未初始化");
            }
            org.flowable.engine.runtime.ProcessInstance flowableInstance = runtimeService.startProcessInstanceByKey(
                definition.getDefinitionKey(),
                businessKey,
                variables
            );

            // 创建流程实例记录
            ProcessInstance instance = new ProcessInstance();
            instance.setProcessDefinitionId(definition.getDefinitionId());
            instance.setBusinessKey(businessKey);
            instance.setBusinessType(businessType);
            instance.setBusinessId(businessId);
            instance.setProcessStatus("RUNNING");
            instance.setStartTime(LocalDateTime.now());
            // 保存Flowable流程实例ID
            instance.setRemark("FlowableInstanceId:" + flowableInstance.getId());
            
            processInstanceMapper.insert(instance);
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("使用Flowable启动流程失败：" + e.getMessage(), e);
        }
    }

    /**
     * 简单方式启动流程（不使用Flowable）
     */
    private ProcessInstance startProcessSimple(ProcessDefinition definition, String businessKey, String businessType, Long businessId) {
        ProcessInstance instance = new ProcessInstance();
        instance.setProcessDefinitionId(definition.getDefinitionId());
        instance.setBusinessKey(businessKey);
        instance.setBusinessType(businessType);
        instance.setBusinessId(businessId);
        instance.setProcessStatus("RUNNING");
        instance.setStartTime(LocalDateTime.now());
        
        processInstanceMapper.insert(instance);
        return instance;
    }

    /**
     * 部署流程定义到Flowable
     */
    private String deployProcessDefinition(ProcessDefinition definition) {
        // TODO: 实现流程定义部署到Flowable
        // 这里需要调用Flowable的RepositoryService部署BPMN XML
        return null;
    }
}



