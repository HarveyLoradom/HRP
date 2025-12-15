package com.hrp.common.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * LocalDateTime 自定义反序列化器
 * 支持 "yyyy-MM-dd" 和 "yyyy-MM-dd HH:mm:ss" 格式
 */
public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String dateStr = p.getText();
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        
        try {
            // 尝试解析 "yyyy-MM-dd" 格式
            if (dateStr.length() == 10) {
                LocalDate date = LocalDate.parse(dateStr);
                return date.atStartOfDay();
            }
            // 尝试解析 "yyyy-MM-dd HH:mm:ss" 格式
            else if (dateStr.length() == 19) {
                return LocalDateTime.parse(dateStr);
            }
            // 尝试解析 ISO 格式
            else {
                return LocalDateTime.parse(dateStr);
            }
        } catch (Exception e) {
            throw new IOException("无法解析日期格式: " + dateStr, e);
        }
    }
}

