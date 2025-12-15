package com.hrp.auth.config;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.io.File;
import java.io.IOException;

/**
 * FreeMarker 配置类
 */
@org.springframework.context.annotation.Configuration
public class FreeMarkerConfig {

    @Bean
    @Primary
    public Configuration freeMarkerConfiguration() throws IOException {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);
        
        // 设置模板加载路径（类路径）
        cfg.setClassForTemplateLoading(this.getClass(), "/templates");
        
        // 设置默认编码
        cfg.setDefaultEncoding("UTF-8");
        
        // 设置模板异常处理
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        
        // 设置日志级别
        cfg.setLogTemplateExceptions(false);
        
        // 设置包装异常
        cfg.setWrapUncheckedExceptions(true);
        
        // 设置空白处理
        cfg.setWhitespaceStripping(true);
        
        return cfg;
    }
}

