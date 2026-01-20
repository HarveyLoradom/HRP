package com.hrp.hr.service;

import com.hrp.common.entity.HrSalConfig;
import java.util.List;

/**
 * 薪酬配置服务接口
 */
public interface HrSalConfigService {
    /**
     * 根据ID查询
     */
    HrSalConfig getById(Integer configId);
    
    /**
     * 根据员工ID查询
     */
    HrSalConfig getByEmpId(Long empId);
    
    /**
     * 分页查询
     */
    com.hrp.common.entity.PageResult<HrSalConfig> getPage(Long page, Long size, Long empId, String empCode, String empName);
    
    /**
     * 保存薪酬配置
     */
    HrSalConfig save(HrSalConfig config);
    
    /**
     * 更新薪酬配置
     */
    HrSalConfig update(HrSalConfig config);
    
    /**
     * 删除薪酬配置
     */
    boolean delete(Integer configId);

    /**
     * 获取所有已有薪酬配置的员工ID列表
     */
    List<Long> getAllEmpIds();

    /**
     * 批量创建薪酬配置（跳过已有配置的员工）
     */
    com.hrp.common.entity.Result<String> batchCreate(List<Long> empIds, String createUser);
}
