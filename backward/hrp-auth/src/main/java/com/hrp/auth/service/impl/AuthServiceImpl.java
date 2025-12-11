package com.hrp.auth.service.impl;

import com.hrp.auth.mapper.CodeMapper;
import com.hrp.auth.mapper.LoginLogMapper;
import com.hrp.auth.service.AuthService;
import com.hrp.auth.service.UserService;
import com.hrp.common.entity.Code;
import com.hrp.common.entity.LoginLog;
import com.hrp.common.entity.User;
import com.hrp.common.entity.UserLogin;
import com.hrp.common.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserService userService;

    @Autowired
    private LoginLogMapper loginLogMapper;

    @Autowired
    private CodeMapper codeMapper;

    private static final int MAX_LOGIN_FAIL_COUNT = 5; // 最大登录失败次数
    private static final int CAPTCHA_TRIGGER_COUNT = 3; // 触发验证码的失败次数
    private static final int LOCK_MINUTES = 10; // 锁定时间（分钟）

    /**
     * 用户登录
     */
    @Override
    @Transactional
    public Map<String, Object> login(String account, String password, String ip, boolean hasCaptcha) {
        // 查询用户
        User user = userService.getByAccount(account);
        if (user == null) {
            recordLoginLog(account, ip, false, "用户不存在");
            return null;
        }

        // 检查用户状态
        if (user.getIsStop() != null && user.getIsStop() == 1) {
            recordLoginLog(account, ip, false, "用户已被停用");
            return null;
        }

        // 获取用户登录信息
        UserLogin userLogin = userService.getUserLogin(user.getId());
        if (userLogin == null) {
            userLogin = userService.getOrCreateUserLogin(user.getId(), user.getAccount());
        }

        // 检查是否锁定
        if (userLogin.getLocked() != null && userLogin.getLocked() == 1) {
            LocalDateTime lockTime = userLogin.getLockTime();
            if (lockTime != null) {
                long minutes = ChronoUnit.MINUTES.between(lockTime, LocalDateTime.now());
                if (minutes < LOCK_MINUTES) {
                    long remainingMinutes = LOCK_MINUTES - minutes;
                    recordLoginLog(account, ip, false, "账户已锁定，剩余" + remainingMinutes + "分钟");
                    return null;
                } else {
                    // 锁定时间已过，自动解锁
                    userService.unlockUser(user.getId());
                    userLogin = userService.getUserLogin(user.getId());
                }
            }
        }

        // 检查是否需要验证码
        if (userLogin.getLoginFailCount() != null && userLogin.getLoginFailCount() >= CAPTCHA_TRIGGER_COUNT && !hasCaptcha) {
            recordLoginLog(account, ip, false, "需要验证码");
            Map<String, Object> result = new HashMap<>();
            result.put("needCaptcha", true);
            result.put("message", "登录失败次数过多，请输入验证码");
            return result;
        }

        // 验证密码
        if (!userService.validatePassword(password, user.getPassword())) {
            int failCount = (userLogin.getLoginFailCount() == null ? 0 : userLogin.getLoginFailCount()) + 1;
            userService.updateLoginFailCount(user.getId(), failCount);

            // 如果失败次数达到上限，锁定账户
            if (failCount >= MAX_LOGIN_FAIL_COUNT) {
                userService.lockUser(user.getId());
                recordLoginLog(account, ip, false, "密码错误，账户已锁定10分钟");
                return null;
            }

            recordLoginLog(account, ip, false, "密码错误，失败次数：" + failCount);
            
            // 如果失败次数达到验证码触发条件，返回需要验证码
            if (failCount >= CAPTCHA_TRIGGER_COUNT) {
                Map<String, Object> result = new HashMap<>();
                result.put("needCaptcha", true);
                result.put("message", "登录失败次数过多，请输入验证码");
                return result;
            }

            return null;
        }

        // 登录成功
        userService.updateLastLoginTime(user.getId());

        // 检查是否需要强制修改密码
        Code forceChangePasswordCode = codeMapper.selectById("FORCE_CHANGE_PASSWORD");
        boolean forceChangePasswordEnabled = forceChangePasswordCode != null && 
                                            "是".equals(forceChangePasswordCode.getCodeValue());
        
        // 检查密码是否为初始密码
        Code resetPasswordCode = codeMapper.selectById("RESET_PASSWORD");
        String defaultPassword = resetPasswordCode != null ? resetPasswordCode.getCodeValue() : "123456";
        boolean isDefaultPassword = userService.validatePassword(defaultPassword, user.getPassword());

        // 如果开启了强制修改密码且密码是初始密码，返回需要修改密码的标识
        if (forceChangePasswordEnabled && isDefaultPassword) {
            Map<String, Object> result = new HashMap<>();
            result.put("needChangePassword", true);
            result.put("message", "检测到您使用的是初始密码，为了账户安全，请先修改密码");
            result.put("userId", user.getId());
            result.put("account", user.getAccount());
            // 不返回token，需要修改密码后才能登录
            return result;
        }

        // 生成token
        String token = JwtUtil.generateToken(user.getId(), user.getAccount());

        // 记录登录日志
        recordLoginLog(account, ip, true, "登录成功");

        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("account", user.getAccount());
        result.put("username", user.getName());
        result.put("realName", user.getName());
        result.put("needCaptcha", false);
        result.put("needChangePassword", false);

        return result;
    }

    /**
     * 记录登录日志
     */
    private void recordLoginLog(String account, String ip, boolean success, String message) {
        LoginLog log = new LoginLog();
        log.setAccount(account);
        log.setIp(ip);
        log.setStatus(success ? 1 : 0);
        log.setMessage(message);
        log.setLoginTime(LocalDateTime.now());
        loginLogMapper.insert(log);
    }
}

