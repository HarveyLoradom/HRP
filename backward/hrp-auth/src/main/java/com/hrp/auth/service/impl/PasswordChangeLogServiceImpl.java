package com.hrp.auth.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hrp.auth.mapper.PasswordChangeLogMapper;
import com.hrp.auth.service.PasswordChangeLogService;
import com.hrp.common.entity.PageResult;
import com.hrp.common.entity.PasswordChangeLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 密码修改日志服务实现类
 */
@Service
public class PasswordChangeLogServiceImpl implements PasswordChangeLogService {

    @Autowired
    private PasswordChangeLogMapper passwordChangeLogMapper;

    @Override
    public PasswordChangeLog getById(Long logId) {
        return passwordChangeLogMapper.selectById(logId);
    }

    @Override
    public PageResult<PasswordChangeLog> getPage(Map<String, Object> params, Long page, Long size) {
        PageHelper.startPage(page.intValue(), size.intValue());
        List<PasswordChangeLog> list = passwordChangeLogMapper.selectPage(params);
        PageInfo<PasswordChangeLog> pageInfo = new PageInfo<>(list);
        
        PageResult<PasswordChangeLog> result = new PageResult<>();
        result.setRecords(list);
        result.setTotal(pageInfo.getTotal());
        result.setCurrent(page);
        result.setSize(size);
        return result;
    }

    @Override
    public boolean logPasswordChange(String userId, Long empId, String empCode, String empName, 
                                     String oldPassword, String newPassword, String changeIp, String changeUser) {
        try {
            // 先查询该用户是否已有日志记录
            PasswordChangeLog existingLog = passwordChangeLogMapper.selectByUserId(userId);
            
            PasswordChangeLog log = new PasswordChangeLog();
            log.setUserId(userId);
            log.setEmpId(empId);
            log.setEmpCode(empCode);
            log.setEmpName(empName);
            log.setOldPassword(oldPassword); // 明文存储
            log.setNewPassword(newPassword); // 明文存储
            log.setChangeTime(LocalDateTime.now());
            log.setChangeIp(changeIp);
            log.setChangeUser(changeUser);
            log.setRemark("用户修改密码");
            
            // 如果已存在记录，则更新；否则插入新记录
            if (existingLog != null) {
                return passwordChangeLogMapper.updateByUserId(log) > 0;
            } else {
                return passwordChangeLogMapper.insert(log) > 0;
            }
        } catch (Exception e) {
            System.err.println("记录密码修改日志失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(Long logId) {
        return passwordChangeLogMapper.deleteById(logId) > 0;
    }
    
    @Override
    public boolean deleteByUserId(String userId) {
        return passwordChangeLogMapper.deleteByUserId(userId) > 0;
    }
}

