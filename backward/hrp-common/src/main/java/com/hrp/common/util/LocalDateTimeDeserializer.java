package com.hrp.common.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * LocalDateTime反序列化器，支持日期字符串（yyyy-MM-dd）和日期时间字符串（yyyy-MM-dd HH:mm:ss）
 */
public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String dateStr = p.getText();
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        
        dateStr = dateStr.trim();
        
        // 如果是日期格式（yyyy-MM-dd），转换为LocalDateTime（时间为00:00:00）
        if (dateStr.length() == 10) {
            LocalDate date = LocalDate.parse(dateStr, DATE_FORMATTER);
            return date.atStartOfDay();
        }
        
        // 如果是日期时间格式（yyyy-MM-dd HH:mm:ss），直接解析
        if (dateStr.length() >= 19) {
            return LocalDateTime.parse(dateStr.substring(0, 19), DATETIME_FORMATTER);
        }
        
        // 其他格式，尝试使用默认解析
        return LocalDateTime.parse(dateStr);
    }
}
