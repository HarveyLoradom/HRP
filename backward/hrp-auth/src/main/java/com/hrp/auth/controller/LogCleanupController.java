package com.hrp.auth.controller;

import com.hrp.auth.service.LoginLogService;
import com.hrp.auth.task.LogFileCleanupTask;
import com.hrp.auth.task.LoginLogCleanupTask;
import com.hrp.common.entity.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 日志清理管理控制器
 * 提供手动触发日志清理任务的接口（包括登录日志和日志文件）
 */
@RestController
@RequestMapping("/admin/log-cleanup")
@CrossOrigin
public class LogCleanupController {
    
    private static final Logger logger = LoggerFactory.getLogger(LogCleanupController.class);
    
    @Autowired
    private LoginLogCleanupTask loginLogCleanupTask;
    
    @Autowired
    private LoginLogService loginLogService;
    
    @Autowired(required = false)
    private LogFileCleanupTask logFileCleanupTask;
    
    /**
     * 手动触发登录日志清理任务
     * 
     * 使用示例（Linux）:
     * curl -X POST http://localhost:8001/admin/log-cleanup/login
     * 
     * @return 清理结果
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> triggerLoginLogCleanup() {
        logger.info("收到手动触发登录日志清理任务的请求");
        
        try {
            // 在新线程中执行，避免阻塞请求
            new Thread(() -> {
                try {
                    loginLogCleanupTask.cleanupOldLogs();
                } catch (Exception e) {
                    logger.error("手动触发登录日志清理任务失败", e);
                }
            }).start();
            
            Map<String, Object> result = new HashMap<>();
            result.put("message", "登录日志清理任务已触发，正在后台执行");
            result.put("status", "running");
            
            return Result.success(result);
        } catch (Exception e) {
            logger.error("触发登录日志清理任务失败", e);
            return Result.error("触发失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取登录日志统计信息
     * 
     * 使用示例（Linux）:
     * curl http://localhost:8001/admin/log-cleanup/login/stats
     * 
     * @return 登录日志统计信息
     */
    @GetMapping("/login/stats")
    public Result<Map<String, Object>> getLoginLogStats() {
        try {
            long totalCount = loginLogService.getLogCount();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalCount", totalCount);
            stats.put("message", "请查看应用日志了解清理任务执行情况");
            
            return Result.success(stats);
        } catch (Exception e) {
            logger.error("获取登录日志统计信息失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }
    
    /**
     * 手动触发日志文件清理任务
     * 
     * 使用示例（Linux）:
     * curl -X POST http://localhost:8001/admin/log-cleanup/file
     * 
     * @return 清理结果
     */
    @PostMapping("/file")
    public Result<Map<String, Object>> triggerLogFileCleanup() {
        logger.info("收到手动触发日志文件清理任务的请求");
        
        if (logFileCleanupTask == null) {
            return Result.error("日志文件清理任务未启用");
        }
        
        try {
            // 在新线程中执行，避免阻塞请求
            new Thread(() -> {
                try {
                    logFileCleanupTask.cleanupLogFiles();
                } catch (Exception e) {
                    logger.error("手动触发日志文件清理任务失败", e);
                }
            }).start();
            
            Map<String, Object> result = new HashMap<>();
            result.put("message", "日志文件清理任务已触发，正在后台执行");
            result.put("status", "running");
            
            return Result.success(result);
        } catch (Exception e) {
            logger.error("触发日志文件清理任务失败", e);
            return Result.error("触发失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取日志清理任务状态
     * 
     * 使用示例（Linux）:
     * curl http://localhost:8001/admin/log-cleanup/status
     * 
     * @return 任务状态信息
     */
    @GetMapping("/status")
    public Result<Map<String, Object>> getCleanupStatus() {
        Map<String, Object> status = new HashMap<>();
        
        status.put("loginLogCleanupEnabled", true);
        
        if (logFileCleanupTask != null) {
            status.put("logFileCleanupEnabled", true);
        } else {
            status.put("logFileCleanupEnabled", false);
        }
        
        status.put("message", "请查看应用日志了解任务执行情况");
        
        return Result.success(status);
    }
}

