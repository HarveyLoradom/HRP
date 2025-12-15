package com.hrp.auth.task;

import com.hrp.auth.mapper.CodeMapper;
import com.hrp.auth.service.LoginLogService;
import com.hrp.common.entity.Code;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 登录日志清理定时任务
 * 定期清理旧的登录日志，避免数据过多
 * 清理天数从 sys_code 表的 LOGIN_LOG_RETAIN_DAYS 参数读取
 */
@Component
public class LoginLogCleanupTask {
    
    private static final Logger logger = LoggerFactory.getLogger(LoginLogCleanupTask.class);
    
    @Autowired
    private LoginLogService loginLogService;
    
    @Autowired
    private CodeMapper codeMapper;
    
    /**
     * 配置参数ID
     */
    private static final String CONFIG_ID = "LOGIN_LOG_RETAIN_DAYS";
    
    /**
     * 默认保留90天的登录日志
     */
    private static final int DEFAULT_RETAIN_DAYS = 90;
    
    /**
     * 每天凌晨2点执行清理任务
     * cron表达式：秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupOldLogs() {
        logger.info("开始执行登录日志清理任务...");
        try {
            // 从 sys_code 表读取清理天数配置
            int retainDays = getRetainDays();
            logger.info("配置的日志保留天数：{}", retainDays);
            
            long beforeCount = loginLogService.getLogCount();
            logger.info("清理前登录日志总数：{}", beforeCount);
            
            int deletedCount = loginLogService.cleanOldLogs(retainDays);
            
            long afterCount = loginLogService.getLogCount();
            logger.info("清理完成，删除了{}条记录，清理后剩余{}条记录", deletedCount, afterCount);
        } catch (Exception e) {
            logger.error("登录日志清理任务执行失败", e);
        }
    }
    
    /**
     * 从 sys_code 表获取日志保留天数
     * @return 保留天数，如果配置不存在或无效，返回默认值90
     */
    private int getRetainDays() {
        try {
            Code config = codeMapper.selectById(CONFIG_ID);
            if (config != null && config.getCodeValue() != null && !config.getCodeValue().trim().isEmpty()) {
                try {
                    int days = Integer.parseInt(config.getCodeValue().trim());
                    if (days > 0) {
                        return days;
                    } else {
                        logger.warn("日志保留天数配置值必须大于0，当前值：{}，使用默认值：{}", days, DEFAULT_RETAIN_DAYS);
                    }
                } catch (NumberFormatException e) {
                    logger.warn("日志保留天数配置值格式错误：{}，使用默认值：{}", config.getCodeValue(), DEFAULT_RETAIN_DAYS);
                }
            } else {
                logger.info("未找到日志保留天数配置（{}），使用默认值：{}", CONFIG_ID, DEFAULT_RETAIN_DAYS);
            }
        } catch (Exception e) {
            logger.error("读取日志保留天数配置失败，使用默认值：{}", DEFAULT_RETAIN_DAYS, e);
        }
        return DEFAULT_RETAIN_DAYS;
    }
}

