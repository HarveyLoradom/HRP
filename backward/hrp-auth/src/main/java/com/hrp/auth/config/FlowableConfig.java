package com.hrp.auth.config;

import org.flowable.common.engine.impl.history.HistoryLevel;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.impl.cfg.StandaloneProcessEngineConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Flowable流程引擎配置
 * 使用手动配置方式，避免 Spring Boot Starter 的自动配置问题
 * 注意：如果 Flowable 功能暂时不需要，可以通过配置禁用
 */
@Configuration
@ConditionalOnProperty(name = "flowable.enabled", havingValue = "true", matchIfMissing = false)
public class FlowableConfig {
    
    // 注意：Flowable 使用手动配置方式，避免自动配置问题
    // 如果需要使用 Flowable，需要手动管理 Flowable 数据库表

    @Bean
    public ProcessEngine processEngine(DataSource dataSource) {
        try {
            // 使用 StandaloneProcessEngineConfiguration 手动配置，避免 Liquibase 问题
            StandaloneProcessEngineConfiguration configuration = new StandaloneProcessEngineConfiguration();
            configuration.setDataSource(dataSource);
            configuration.setDatabaseType("mysql");
            // 禁用数据库架构更新，使用手动管理的表结构
            configuration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_FALSE);
            // 禁用异步执行器
            configuration.setAsyncExecutorActivate(false);
            // 设置历史级别
            configuration.setHistoryLevel(HistoryLevel.FULL);
            
            return configuration.buildProcessEngine();
        } catch (Exception e) {
            // 如果 Flowable 初始化失败，记录日志但不阻止应用启动
            System.err.println("Flowable 初始化失败，流程功能将不可用: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}

