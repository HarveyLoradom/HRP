package com.hrp.auth.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.util.RandomUtil;
import com.hrp.auth.service.CaptchaService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CaptchaServiceImpl implements CaptchaService {

    // 存储验证码，实际生产环境应该使用Redis
    private static final ConcurrentHashMap<String, String> captchaStore = new ConcurrentHashMap<>();
    private static final long CAPTCHA_EXPIRE_TIME = 5 * 60 * 1000; // 5分钟过期

    /**
     * 生成验证码
     */
    @Override
    public Map<String, String> generateCaptcha() {
        // 生成4位数字验证码
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(120, 40, 4, 50);
        String code = lineCaptcha.getCode();
        String key = RandomUtil.randomString(32);

        // 存储验证码（实际应该存到Redis，设置过期时间）
        captchaStore.put(key, code);

        // 5分钟后自动删除
        new Thread(() -> {
            try {
                Thread.sleep(CAPTCHA_EXPIRE_TIME);
                captchaStore.remove(key);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        Map<String, String> result = new HashMap<>();
        result.put("key", key);
        result.put("image", "data:image/png;base64," + lineCaptcha.getImageBase64());
        return result;
    }

    /**
     * 验证验证码
     */
    @Override
    public boolean validateCaptcha(String key, String code) {
        String storedCode = captchaStore.get(key);
        if (storedCode == null) {
            return false;
        }
        
        boolean valid = storedCode.equalsIgnoreCase(code);
        // 验证后删除验证码（一次性使用）
        captchaStore.remove(key);
        return valid;
    }
}

