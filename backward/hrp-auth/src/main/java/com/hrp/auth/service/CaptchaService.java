package com.hrp.auth.service;

import java.util.Map;

/**
 * 验证码服务接口
 */
public interface CaptchaService {
    /**
     * 生成验证码
     */
    Map<String, String> generateCaptcha();

    /**
     * 验证验证码
     */
    boolean validateCaptcha(String key, String code);
}

