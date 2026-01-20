package com.hrp.auth.service;

import com.hrp.common.entity.PasswordChangeLog;
import com.hrp.common.entity.PageResult;

import java.util.Map;

/**
 * 密码修改日志服务接口
 */
public interface PasswordChangeLogService {
    /**
     * 根据ID查询
     */
    PasswordChangeLog getById(Long logId);
    
    /**
     * 分页查询
     */
    PageResult<PasswordChangeLog> getPage(Map<String, Object> params, Long page, Long size);
    
    /**
     * 记录密码修改日志
     */
    boolean logPasswordChange(String userId, Long empId, String empCode, String empName, 
                             String oldPassword, String newPassword, String changeIp, String changeUser);
    
    /**
     * 删除日志
     */
    boolean delete(Long logId);
    
    /**
     * 根据用户ID删除所有密码修改日志
     */
    boolean deleteByUserId(String userId);
}

