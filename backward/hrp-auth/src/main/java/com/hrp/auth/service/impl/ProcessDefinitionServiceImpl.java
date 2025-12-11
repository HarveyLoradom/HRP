package com.hrp.auth.service.impl;

import com.hrp.auth.mapper.ProcessDefinitionMapper;
import com.hrp.auth.service.ProcessDefinitionService;
import com.hrp.common.entity.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
}



