package com.hrp.hr.service.impl;

import com.hrp.common.entity.HrSalConfig;
import com.hrp.common.entity.PageResult;
import com.hrp.common.exception.BusinessException;
import com.hrp.hr.mapper.HrSalConfigMapper;
import com.hrp.hr.service.HrSalConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HrSalConfigServiceImpl implements HrSalConfigService {

    @Autowired
    private HrSalConfigMapper hrSalConfigMapper;

    @Override
    public HrSalConfig getById(Integer configId) {
        return hrSalConfigMapper.selectById(configId);
    }

    @Override
    public HrSalConfig getByEmpId(Long empId) {
        return hrSalConfigMapper.selectByEmpId(empId);
    }

    @Override
    public PageResult<HrSalConfig> getPage(Long page, Long size, Long empId, String empCode, String empName) {
        List<HrSalConfig> allList = hrSalConfigMapper.selectByConditions(empId, empCode, empName);
        Long total = (long) allList.size();
        
        int start = (int) ((page - 1) * size);
        if (start >= allList.size()) {
            return new PageResult<>(java.util.Collections.emptyList(), total, size, page);
        }
        int end = Math.min(start + size.intValue(), allList.size());
        List<HrSalConfig> list = allList.subList(start, end);
        
        return new PageResult<>(list, total, size, page);
    }

    @Override
    @Transactional
    public HrSalConfig save(HrSalConfig config) {
        if (config.getEmpId() == null) {
            throw new BusinessException("员工ID不能为空");
        }
        // 检查是否已存在该员工的配置
        HrSalConfig existing = hrSalConfigMapper.selectByEmpId(config.getEmpId());
        if (existing != null) {
            throw new BusinessException("该员工已存在薪酬配置，请使用更新功能");
        }
        if (config.getBasicSalary() == null || config.getBasicSalary().doubleValue() < 0) {
            throw new BusinessException("基本工资不能为空且必须大于等于0");
        }
        hrSalConfigMapper.insert(config);
        return config;
    }

    @Override
    @Transactional
    public HrSalConfig update(HrSalConfig config) {
        if (config.getConfigId() == null) {
            throw new BusinessException("配置ID不能为空");
        }
        if (config.getBasicSalary() != null && config.getBasicSalary().doubleValue() < 0) {
            throw new BusinessException("基本工资必须大于等于0");
        }
        hrSalConfigMapper.updateById(config);
        return hrSalConfigMapper.selectById(config.getConfigId());
    }

    @Override
    @Transactional
    public boolean delete(Integer configId) {
        return hrSalConfigMapper.deleteById(configId) > 0;
    }

    @Override
    public List<Long> getAllEmpIds() {
        return hrSalConfigMapper.selectAllEmpIds();
    }

    @Override
    @Transactional
    public com.hrp.common.entity.Result<String> batchCreate(List<Long> empIds, String createUser) {
        if (empIds == null || empIds.isEmpty()) {
            return com.hrp.common.entity.Result.error("请选择至少一个员工");
        }

        int successCount = 0;
        int skipCount = 0;
        int failCount = 0;
        StringBuilder errorMsg = new StringBuilder();

        // 获取已有配置的员工ID列表
        List<Long> existingEmpIds = hrSalConfigMapper.selectAllEmpIds();

        for (Long empId : empIds) {
            try {
                // 检查是否已有配置
                if (existingEmpIds.contains(empId)) {
                    skipCount++;
                    continue;
                }

                // 创建新配置（使用默认值）
                HrSalConfig config = new HrSalConfig();
                config.setEmpId(empId);
                config.setBasicSalary(java.math.BigDecimal.ZERO);
                config.setPostAllowance(java.math.BigDecimal.ZERO);
                config.setSocialSecurity(java.math.BigDecimal.ZERO);
                config.setProvidentFund(java.math.BigDecimal.ZERO);
                config.setTaxThreshold(new java.math.BigDecimal("5000.00"));
                config.setCreateUser(createUser != null ? createUser : "SYSTEM");

                hrSalConfigMapper.insert(config);
                successCount++;
            } catch (Exception e) {
                failCount++;
                errorMsg.append("员工ID ").append(empId).append(": ").append(e.getMessage()).append("\n");
            }
        }

        String message = String.format("批量创建完成：成功%d条，跳过%d条（已有配置），失败%d条", 
                successCount, skipCount, failCount);
        if (failCount > 0 && errorMsg.length() > 0) {
            message += "\n错误详情：\n" + errorMsg.toString();
        }

        if (failCount == 0 && successCount > 0) {
            return com.hrp.common.entity.Result.success(message);
        } else if (successCount > 0) {
            return com.hrp.common.entity.Result.error(message);
        } else {
            return com.hrp.common.entity.Result.error(message);
        }
    }
}
