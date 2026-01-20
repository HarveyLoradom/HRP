package com.hrp.contract.task;

import com.hrp.contract.mapper.PactMainMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 合同执行状态自动更新定时任务
 * 根据开始时间和结束时间自动更新执行状态：
 * - 当前时间 >= 开始时间：执行状态改为履约中（EXECUTING）
 * - 当前时间 >= 结束时间：执行状态改为已履约（COMPLETED）
 * 只更新非人工修改的合同（is_manual_modify = 0）
 */
@Component
public class ContractExecutionStatusUpdateTask {
    
    private static final Logger logger = LoggerFactory.getLogger(ContractExecutionStatusUpdateTask.class);
    
    @Autowired
    private PactMainMapper pactMainMapper;
    
    /**
     * 每小时执行一次更新任务
     * cron表达式：秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 * * * ?")
    @Transactional
    public void updateExecutionStatus() {
        logger.info("开始执行合同执行状态自动更新任务...");
        try {
            LocalDateTime now = LocalDateTime.now();
            
            // 更新待履约和履约中的合同
            // 当前时间 >= 开始时间，且执行状态为待履约或履约中的，改为履约中
            // 注意：只更新非人工修改的合同
            int updatedToExecuting = pactMainMapper.updateExecutionStatusToExecuting(now);
            logger.info("更新为履约中的合同数量：{}", updatedToExecuting);
            
            // 当前时间 >= 结束时间，且执行状态为履约中的，改为已履约
            // 注意：只更新非人工修改的合同
            int updatedToCompleted = pactMainMapper.updateExecutionStatusToCompleted(now);
            logger.info("更新为已履约的合同数量：{}", updatedToCompleted);
            
            logger.info("合同执行状态自动更新任务执行完成");
        } catch (Exception e) {
            logger.error("合同执行状态自动更新任务执行失败", e);
        }
    }
}

