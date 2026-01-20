package com.hrp.common.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * 日志文件清理定时任务
 * 每周执行一次，删除超过指定天数的日志文件
 */
@Component
@ConditionalOnProperty(name = "logging.cleanup.enabled", havingValue = "true", matchIfMissing = true)
public class LogFileCleanupTask {
    
    private static final Logger logger = LoggerFactory.getLogger(LogFileCleanupTask.class);
    
    /**
     * 日志文件路径（从配置中读取，默认：F:/data/logs）
     */
    @Value("${logging.file.path:F:/data/logs}")
    private String logPath;
    
    /**
     * 日志保留天数（从配置中读取，默认：30天）
     */
    @Value("${logging.cleanup.retain-days:30}")
    private int retainDays;
    
    /**
     * 是否启用清理任务（从配置中读取，默认：true）
     */
    @Value("${logging.cleanup.enabled:true}")
    private boolean enabled;
    
    /**
     * 应用名称（从配置中读取，用于创建服务专用日志目录）
     */
    @Value("${spring.application.name:hrp-app}")
    private String appName;
    
    /**
     * 每周一凌晨3点执行清理任务
     * cron表达式：秒 分 时 日 月 周
     * 0 0 3 ? * MON 表示每周一凌晨3点
     */
    @Scheduled(cron = "${logging.cleanup.cron:0 0 3 ? * MON}")
    public void cleanupLogFiles() {
        if (!enabled) {
            logger.debug("日志文件清理任务已禁用");
            return;
        }
        
        logger.info("开始执行日志文件清理任务...");
        logger.info("日志路径：{}，保留天数：{}", logPath, retainDays);
        
        try {
            Path logDir = Paths.get(logPath);
            
            // 检查日志目录是否存在
            if (!Files.exists(logDir) || !Files.isDirectory(logDir)) {
                logger.warn("日志目录不存在或不是目录：{}", logPath);
                return;
            }
            
            // 计算截止日期（retainDays天前）
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retainDays);
            Instant cutoffInstant = cutoffDate.atZone(ZoneId.systemDefault()).toInstant();
            
            int deletedCount = 0;
            long deletedSize = 0;
            List<String> deletedFiles = new ArrayList<>();
            
            // 遍历日志目录下的所有子目录（每个服务一个目录）
            try (Stream<Path> dirs = Files.list(logDir)) {
                for (Path serviceDir : dirs.toArray(Path[]::new)) {
                    if (Files.isDirectory(serviceDir)) {
                        // 清理该服务目录下的日志文件
                        List<String> serviceDeletedFiles = new ArrayList<>();
                        int serviceDeletedCount = cleanupDirectory(serviceDir, cutoffInstant, serviceDeletedFiles);
                        deletedCount += serviceDeletedCount;
                        
                        // 计算删除的文件大小
                        for (String filePath : serviceDeletedFiles) {
                            try {
                                File file = new File(filePath);
                                if (file.exists()) {
                                    long fileSize = file.length();
                                    deletedSize += fileSize;
                                    deletedFiles.add(filePath);
                                }
                            } catch (Exception e) {
                                logger.warn("计算文件大小失败：{}", filePath, e);
                            }
                        }
                        
                        if (serviceDeletedCount > 0) {
                            logger.info("服务目录 {} 清理完成，删除了 {} 个文件", serviceDir.getFileName(), serviceDeletedCount);
                        }
                    }
                }
            }
            
            logger.info("日志文件清理完成，删除了{}个文件，释放空间：{}MB", 
                    deletedCount, deletedSize / 1024 / 1024);
            
            if (!deletedFiles.isEmpty() && logger.isDebugEnabled()) {
                logger.debug("已删除的文件列表：{}", deletedFiles);
            }
            
        } catch (Exception e) {
            logger.error("日志文件清理任务执行失败", e);
        }
    }
    
    /**
     * 清理指定目录下的日志文件
     * @param directory 目录路径
     * @param cutoffInstant 截止时间（早于此时间的文件将被删除）
     * @param deletedFiles 已删除文件列表（用于记录）
     * @return 删除的文件数量
     */
    private int cleanupDirectory(Path directory, Instant cutoffInstant, List<String> deletedFiles) {
        int deletedCount = 0;
        
        try (Stream<Path> files = Files.list(directory)) {
            for (Path file : files.toArray(Path[]::new)) {
                if (Files.isRegularFile(file)) {
                    String fileName = file.getFileName().toString();
                    // 只处理日志文件（以.log结尾或以log开头或以error-开头）
                    if (fileName.endsWith(".log") || fileName.startsWith("log") || fileName.startsWith("error-")) {
                        try {
                            // 获取文件最后修改时间
                            FileTime fileTime = Files.getLastModifiedTime(file);
                            Instant fileInstant = fileTime.toInstant();
                            
                            // 如果文件修改时间早于截止时间，删除该文件
                            if (fileInstant.isBefore(cutoffInstant)) {
                                long fileSize = Files.size(file);
                                Files.delete(file);
                                deletedCount++;
                                deletedFiles.add(file.toString());
                                logger.debug("删除过期日志文件：{}，大小：{}KB，最后修改时间：{}", 
                                        fileName, fileSize / 1024, 
                                        java.time.LocalDateTime.ofInstant(fileTime.toInstant(), 
                                                java.time.ZoneId.systemDefault()));
                            }
                        } catch (IOException e) {
                            logger.warn("删除日志文件失败：{}，错误：{}", file, e.getMessage());
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.warn("遍历日志目录失败：{}，错误：{}", directory, e.getMessage());
        }
        
        return deletedCount;
    }
    
    /**
     * 初始化日志目录（PostConstruct方式，应用启动时执行一次）
     * 在应用启动时自动创建日志目录（如果不存在），并重命名现有日志文件为带日期的格式
     */
    @javax.annotation.PostConstruct
    public void initLogDirectory() {
        try {
            Path logDir = Paths.get(logPath);
            // 创建日志根目录（如果不存在）
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
                logger.info("创建日志根目录：{}", logPath);
            }
            
            // 创建当前服务的日志目录（如果不存在）
            Path serviceLogDir = logDir.resolve(appName);
            if (!Files.exists(serviceLogDir)) {
                Files.createDirectories(serviceLogDir);
                logger.info("创建服务日志目录：{}", serviceLogDir);
            }
            
            // 确保日志目录可写
            if (!Files.isWritable(serviceLogDir)) {
                logger.warn("日志目录不可写：{}", serviceLogDir);
                return;
            }
            
            // 重命名现有日志文件为带日期的格式（仅处理旧的 log.log 和 error.log 文件）
            renameLogFiles(serviceLogDir);
            
            logger.info("日志目录初始化成功：{}", serviceLogDir);
        } catch (IOException e) {
            logger.error("创建日志目录失败：{}", logPath, e);
        }
    }
    
    /**
     * 将现有的日志文件重命名为带日期的格式
     * 例如：log.log -> log2025011901.log
     * 注意：如果log.log文件正在被写入（最近修改），则暂时不重命名，等待滚动时自动重命名
     */
    private void renameLogFiles(Path serviceLogDir) {
        try {
            String dateStr = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            
            // 重命名 log.log -> log年月日01.log
            Path logFile = serviceLogDir.resolve("log.log");
            if (Files.exists(logFile) && Files.isRegularFile(logFile)) {
                try {
                    // 检查文件的最后修改时间
                    java.time.LocalDateTime fileTime = java.time.LocalDateTime.ofInstant(
                            Files.getLastModifiedTime(logFile).toInstant(),
                            java.time.ZoneId.systemDefault());
                    
                    // 如果文件是今天创建的且很新（最近5分钟内），说明可能正在被写入，暂不重命名
                    // 或者文件大小很小（小于100字节），可能是刚创建的，暂不重命名
                    long fileSize = Files.size(logFile);
                    long minutesAgo = java.time.Duration.between(fileTime, now).toMinutes();
                    
                    if (minutesAgo < 5 && fileSize < 100) {
                        logger.debug("日志文件 {} 可能正在被写入，暂不重命名", logFile.getFileName());
                        // 等待一会儿，让logback完成初始化，然后异步重命名
                        java.util.concurrent.CompletableFuture.runAsync(() -> {
                            try {
                                Thread.sleep(3000); // 等待3秒
                                renameLogFileIfNeeded(logFile, dateStr, serviceLogDir, "log");
                            } catch (Exception e) {
                                logger.warn("异步重命名日志文件失败：{}", e.getMessage());
                            }
                        });
                    } else {
                        // 文件不是刚创建的，直接重命名
                        renameLogFileIfNeeded(logFile, dateStr, serviceLogDir, "log");
                    }
                } catch (Exception e) {
                    logger.warn("检查日志文件 {} 失败：{}", logFile.getFileName(), e.getMessage());
                }
            }
            
            // 重命名 error.log -> error-年月日01.log
            Path errorFile = serviceLogDir.resolve("error.log");
            if (Files.exists(errorFile) && Files.isRegularFile(errorFile)) {
                try {
                    java.time.LocalDateTime fileTime = java.time.LocalDateTime.ofInstant(
                            Files.getLastModifiedTime(errorFile).toInstant(),
                            java.time.ZoneId.systemDefault());
                    long fileSize = Files.size(errorFile);
                    long minutesAgo = java.time.Duration.between(fileTime, now).toMinutes();
                    
                    if (minutesAgo < 5 && fileSize < 100) {
                        logger.debug("错误日志文件 {} 可能正在被写入，暂不重命名", errorFile.getFileName());
                        java.util.concurrent.CompletableFuture.runAsync(() -> {
                            try {
                                Thread.sleep(3000);
                                renameLogFileIfNeeded(errorFile, dateStr, serviceLogDir, "error-");
                            } catch (Exception e) {
                                logger.warn("异步重命名错误日志文件失败：{}", e.getMessage());
                            }
                        });
                    } else {
                        renameLogFileIfNeeded(errorFile, dateStr, serviceLogDir, "error-");
                    }
                } catch (Exception e) {
                    logger.warn("检查错误日志文件 {} 失败：{}", errorFile.getFileName(), e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.warn("重命名日志文件失败：{}", e.getMessage());
        }
    }
    
    /**
     * 重命名日志文件（如果需要）
     */
    private void renameLogFileIfNeeded(Path logFile, String dateStr, Path serviceLogDir, String prefix) {
        try {
            String newFileName;
            if ("error-".equals(prefix)) {
                newFileName = String.format("error-%s01.log", dateStr);
            } else {
                newFileName = String.format("log%s01.log", dateStr);
            }
            Path newLogFile = serviceLogDir.resolve(newFileName);
            
            // 如果目标文件已存在且是今天的文件，检查文件大小，可能需要合并或跳过
            if (Files.exists(newLogFile)) {
                if (isTodayFile(newLogFile)) {
                    // 如果目标文件已存在，且是今天的，检查是否需要合并
                    long existingSize = Files.size(newLogFile);
                    long sourceSize = Files.size(logFile);
                    if (sourceSize > 0 && existingSize > 0) {
                        // 两个文件都有内容，暂时不重命名，让logback自己处理
                        logger.debug("目标日志文件已存在且有内容，暂不重命名：{}", newFileName);
                        return;
                    }
                }
            }
            
            // 尝试重命名
            if (Files.exists(logFile)) {
                Files.move(logFile, newLogFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                logger.info("重命名日志文件：{} -> {}", logFile.getFileName(), newFileName);
            }
        } catch (Exception e) {
            logger.warn("重命名日志文件失败：{} -> {}，错误：{}", logFile.getFileName(), dateStr, e.getMessage());
        }
    }
    
    /**
     * 检查文件是否是今天的文件（通过文件名判断）
     */
    private boolean isTodayFile(Path file) {
        String fileName = file.getFileName().toString();
        String dateStr = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        return fileName.contains(dateStr);
    }
}

