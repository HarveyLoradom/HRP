package com.hrp.auth.service.impl;

import com.hrp.auth.mapper.CodeMapper;
import com.hrp.auth.mapper.LoginLogMapper;
import com.hrp.auth.service.AuthService;
import com.hrp.auth.service.UserService;
import com.hrp.auth.service.UserEmployeeService;
import com.hrp.common.entity.Code;
import com.hrp.common.entity.Employee;
import com.hrp.common.entity.LoginLog;
import com.hrp.common.entity.User;
import com.hrp.common.entity.UserLogin;
import com.hrp.common.exception.BusinessException;
import com.hrp.common.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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

    @Autowired
    private UserEmployeeService userEmployeeService;

    private static final int MAX_LOGIN_FAIL_COUNT = 5; // 最大登录失败次数
    private static final int CAPTCHA_TRIGGER_COUNT = 3; // 触发验证码的失败次数
    private static final int LOCK_MINUTES = 30; // 锁定时间（分钟）

    /**
     * 用户登录
     */
    @Override
    @Transactional
    public Map<String, Object> login(String account, String password, String ip, boolean hasCaptcha) {
        // 查询用户
        User user = userService.getByAccount(account);
        if (user == null) {
            recordLoginLog(account, ip, false, "用户不存在或密码错误");
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 检查用户状态（sys_user表的is_stop字段）
        if (user.getIsStop() != null && user.getIsStop() == 1) {
            recordLoginLog(account, ip, false, "该用户已被停用");
            throw new BusinessException(403, "该用户已被停用");
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
                    throw new BusinessException(423, "账户已锁定，剩余" + remainingMinutes + "分钟");
                } else {
                    // 锁定时间已过，自动解锁并重置失败次数
                    userService.unlockUser(user.getId());
                    // 重新获取用户登录信息
                    userLogin = userService.getUserLogin(user.getId());
                }
            }
        }

        // 验证密码
        if (!userService.validatePassword(password, user.getPassword())) {
            // 获取当前的失败次数
            int currentFailCount = userLogin.getLoginFailCount() == null ? 0 : userLogin.getLoginFailCount();
            int newFailCount = currentFailCount + 1;
            
            // 更新失败次数到数据库
            userService.updateLoginFailCount(user.getId(), newFailCount);
            
            recordLoginLog(account, ip, false, "密码错误，失败次数：" + newFailCount);

            // 如果失败次数达到上限（5次），锁定账户
            if (newFailCount >= MAX_LOGIN_FAIL_COUNT) {
                userService.lockUser(user.getId());
                recordLoginLog(account, ip, false, "密码错误，账户已锁定30分钟");
                throw new BusinessException(423, "密码错误次数过多，账户已锁定30分钟");
            }
            
            // 如果失败次数达到验证码触发条件（3次），返回需要验证码
            if (newFailCount >= CAPTCHA_TRIGGER_COUNT) {
                Map<String, Object> result = new HashMap<>();
                result.put("needCaptcha", true);
                result.put("message", "密码错误，登录失败次数过多（" + newFailCount + "次），请输入验证码");
                return result;
            }

            throw new BusinessException(401, "用户名或密码错误");
        }
        
        // 密码验证成功，重新获取用户登录信息检查是否需要验证码
        userLogin = userService.getUserLogin(user.getId());
        if (userLogin != null && userLogin.getLoginFailCount() != null && 
            userLogin.getLoginFailCount() >= CAPTCHA_TRIGGER_COUNT && !hasCaptcha) {
            recordLoginLog(account, ip, false, "需要验证码");
            Map<String, Object> result = new HashMap<>();
            result.put("needCaptcha", true);
            result.put("message", "登录失败次数过多，请输入验证码");
            return result;
        }

        // 登录成功
        userService.updateLastLoginTime(user.getId());

        // 检查是否需要强制修改密码
        Code forceChangePasswordCode = null;
        try {
            forceChangePasswordCode = codeMapper.selectById("FORCE_CHANGE_PASSWORD");
        } catch (Exception e) {
            // 如果查询失败，忽略此配置，不影响登录
            System.err.println("查询强制修改密码配置失败: " + e.getMessage());
        }
        boolean forceChangePasswordEnabled = forceChangePasswordCode != null && 
                                            "是".equals(forceChangePasswordCode.getCodeValue());
        
        // 检查密码是否为初始密码
        Code resetPasswordCode = null;
        try {
            resetPasswordCode = codeMapper.selectById("RESET_PASSWORD");
        } catch (Exception e) {
            // 如果查询失败，使用默认密码
            System.err.println("查询重置密码配置失败: " + e.getMessage());
        }
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

        // 查询员工信息，获取empName
        Employee employee = null;
        String empName = null;
        try {
            employee = userEmployeeService.getEmployeeByCode(user.getAccount());
            if (employee != null) {
                empName = employee.getEmpName();
            }
        } catch (Exception e) {
            // 如果查询员工信息失败，不影响登录，只记录错误
            System.err.println("查询员工信息失败: " + e.getMessage());
        }

        // 记录登录日志
        recordLoginLog(account, ip, true, "登录成功");

        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("account", user.getAccount());
        result.put("username", user.getName());
        result.put("realName", empName != null ? empName : user.getName()); // 优先使用员工姓名
        result.put("empName", empName); // 员工姓名
        result.put("needCaptcha", false);
        result.put("needChangePassword", false);

        return result;
    }

    /**
     * 记录登录日志（使用独立事务，确保即使主事务回滚也能保存）
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordLoginLog(String account, String ip, boolean success, String message) {
        try {
        LoginLog log = new LoginLog();
        log.setAccount(account);
        log.setIp(ip);
        log.setStatus(success ? 1 : 0);
        log.setMessage(message);
        log.setLoginTime(LocalDateTime.now());
        loginLogMapper.insert(log);
        } catch (Exception e) {
            // 记录日志失败不应该影响主流程，只记录错误
            System.err.println("记录登录日志失败: " + e.getMessage());
        }
    }
}

