package com.hrp.auth.service.impl;

import com.hrp.auth.mapper.LoginLogMapper;
import com.hrp.auth.service.LoginLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 登录日志服务实现类
 */
@Service
public class LoginLogServiceImpl implements LoginLogService {
    
    private static final Logger logger = LoggerFactory.getLogger(LoginLogServiceImpl.class);
    
    @Autowired
    private LoginLogMapper loginLogMapper;
    
    /**
     * 清理指定天数之前的登录日志
     * @param days 保留最近多少天的日志，例如90表示只保留最近90天的日志
     * @return 删除的记录数
     */
    @Override
    @Transactional
    public int cleanOldLogs(int days) {
        if (days < 1) {
            logger.warn("清理天数必须大于0，当前值：{}", days);
            return 0;
        }
        
        LocalDateTime beforeTime = LocalDateTime.now().minusDays(days);
        logger.info("开始清理{}天之前的登录日志，截止时间：{}", days, beforeTime);
        
        int deletedCount = loginLogMapper.deleteBeforeTime(beforeTime);
        
        logger.info("清理完成，删除了{}条登录日志记录", deletedCount);
        return deletedCount;
    }
    
    /**
     * 获取登录日志总数
     * @return 总记录数
     */
    @Override
    public long getLogCount() {
        return loginLogMapper.count();
    }
}

