package com.hrp.auth.service.impl;

import com.hrp.auth.mapper.ProcessInstanceMapper;
import com.hrp.auth.mapper.ProcessVariableMapper;
import com.hrp.auth.service.ProcessInstanceService;
import com.hrp.common.entity.ProcessInstance;
import com.hrp.common.entity.ProcessVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 流程实例服务实现类
 */
@Service
public class ProcessInstanceServiceImpl implements ProcessInstanceService {

    @Autowired
    private ProcessInstanceMapper processInstanceMapper;

    @Autowired
    private ProcessVariableMapper processVariableMapper;

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
}



