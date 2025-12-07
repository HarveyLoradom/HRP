package com.hrp.auth.service;

import java.util.Map;

/**
 * 认证服务接口
 */
public interface AuthService {
    /**
     * 用户登录
     */
    Map<String, Object> login(String account, String password, String ip, boolean hasCaptcha);
}

