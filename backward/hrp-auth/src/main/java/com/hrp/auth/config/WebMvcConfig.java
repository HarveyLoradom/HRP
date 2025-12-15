package com.hrp.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Web MVC 配置类
 * 用于配置静态资源访问
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload.base-path:${user.dir}/uploads}")
    private String basePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 规范化路径 - 确保路径格式正确
        String normalizedPath = normalizePath(basePath);
        
        // Windows路径需要特殊处理：file:协议后面需要三个斜杠，或者使用反斜杠
        // file:///F:/data/uploads/ 或 file:F:/data/uploads/
        String fileUrl = normalizedPath;
        if (!fileUrl.startsWith("file:")) {
            // Windows路径：file:F:/data/uploads/
            // Linux路径：file:/var/uploads/
            if (fileUrl.contains(":")) {
                // Windows路径
                fileUrl = "file:" + fileUrl.replace("\\", "/");
            } else {
                // Linux路径
                fileUrl = "file:" + fileUrl;
            }
        }
        
        // 确保路径以 / 结尾（Windows路径已经处理过）
        if (!fileUrl.endsWith("/")) {
            fileUrl += "/";
        }
        
        // 配置静态资源访问路径
        // 访问路径：/uploads/** 映射到实际文件目录
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(fileUrl)
                .setCachePeriod(3600); // 缓存1小时
        
        // 同时支持 /api/uploads/** 路径（前端代理后的路径）
        registry.addResourceHandler("/api/uploads/**")
                .addResourceLocations(fileUrl)
                .setCachePeriod(3600); // 缓存1小时
        
        System.out.println("=== 静态资源映射配置完成 ===");
        System.out.println("  映射路径: /uploads/** 和 /api/uploads/**");
        System.out.println("  文件系统路径: " + normalizedPath);
        System.out.println("  资源位置: " + fileUrl);
        System.out.println("  示例: /api/uploads/employee-photos/xxx.jpg -> " + fileUrl + "employee-photos/xxx.jpg");
        System.out.println("============================");
    }

    /**
     * 规范化路径，处理 ${user.dir} 等占位符
     */
    private String normalizePath(String path) {
        if (path == null || path.isEmpty()) {
            return System.getProperty("user.dir") + File.separator + "uploads";
        }
        // 替换 ${user.dir} 占位符
        String normalized = path.replace("${user.dir}", System.getProperty("user.dir"));
        // 统一路径分隔符为正斜杠（URL 使用正斜杠）
        normalized = normalized.replace("\\", "/");
        // 确保路径以 / 结尾（Windows 下需要处理）
        if (!normalized.endsWith("/") && !normalized.endsWith("\\")) {
            normalized += "/";
        }
        return normalized;
    }
}

