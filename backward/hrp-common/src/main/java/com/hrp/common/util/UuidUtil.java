package com.hrp.common.util;

import java.util.UUID;

/**
 * UUID工具类
 */
public class UuidUtil {
    /**
     * 生成UUID（去除横线，32位）
     */
    public static String generateUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * 生成标准UUID（带横线，36位）
     */
    public static String generateStandardUuid() {
        return UUID.randomUUID().toString();
    }
}

