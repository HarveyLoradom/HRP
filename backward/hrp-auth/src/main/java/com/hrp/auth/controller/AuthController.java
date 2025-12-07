package com.hrp.auth.controller;

import com.hrp.auth.service.AuthService;
import com.hrp.auth.service.CaptchaService;
import com.hrp.common.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private CaptchaService captchaService;

    @Autowired
    private com.hrp.auth.service.UserService userService;

    /**
     * 生成验证码
     */
    @GetMapping("/captcha")
    public Result<Map<String, String>> generateCaptcha() {
        Map<String, String> captcha = captchaService.generateCaptcha();
        return Result.success(captcha);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params, HttpServletRequest request) {
        String account = params.get("account");
        String password = params.get("password");
        String captchaCode = params.get("captchaCode");
        String captchaKey = params.get("captchaKey");

        // 兼容旧的前端可能传workNo
        if (account == null) {
            account = params.get("workNo");
        }

        // 验证验证码
        if (captchaCode != null && captchaKey != null) {
            if (!captchaService.validateCaptcha(captchaKey, captchaCode)) {
                return Result.error("验证码错误");
            }
        }


        String ip = getClientIp(request);

        // 执行登录
        Map<String, Object> result = authService.login(account, password, ip, captchaCode != null);

        if (result == null) {
            return Result.error("登录失败");
        }

        return Result.success(result);
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        // 这里可以实现token黑名单等功能
        return Result.success();
    }

    /**
     * 修改密码
     */
    @PostMapping("/password/change")
    public Result<Void> changePassword(@RequestBody Map<String, String> params, 
                                       @RequestHeader(value = "Authorization", required = false) String token) {
        String userId = params.get("userId");
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");

        if (userId == null || oldPassword == null || newPassword == null) {
            return Result.error("参数不完整");
        }

        if (newPassword.length() < 6 || newPassword.length() > 20) {
            return Result.error("新密码长度必须在6-20位之间");
        }

        boolean success = userService.changePassword(userId, oldPassword, newPassword);
        if (success) {
            return Result.success();
        } else {
            return Result.error("原密码错误或修改失败");
        }
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
