package com.hrp.auth.service;

/**
 * 登录日志服务接口
 */
public interface LoginLogService {
    /**
     * 清理指定天数之前的登录日志
     * @param days 保留最近多少天的日志，例如90表示只保留最近90天的日志
     * @return 删除的记录数
     */
    int cleanOldLogs(int days);
    
    /**
     * 获取登录日志总数
     * @return 总记录数
     */
    long getLogCount();
}

