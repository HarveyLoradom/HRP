package com.hrp.auth.service.impl;

import com.hrp.auth.mapper.UserMapper;
import com.hrp.auth.mapper.UserLoginMapper;
import com.hrp.auth.service.UserService;
import com.hrp.common.entity.User;
import com.hrp.common.entity.UserLogin;
import com.hrp.common.util.PasswordUtil;
import com.hrp.common.util.UuidUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserLoginMapper userLoginMapper;

    /**
     * 根据账号查询用户
     */
    @Override
    public User getByAccount(String account) {
        User user = userMapper.selectByAccount(account);
        // 只返回未停用的用户
        if (user != null && user.getIsStop() != null && user.getIsStop() == 1) {
            return null;
        }
        return user;
    }

    /**
     * 获取或创建用户登录信息
     */
    @Override
    public UserLogin getOrCreateUserLogin(String userId, String account) {
        UserLogin userLogin = userLoginMapper.selectByUserId(userId);
        
        if (userLogin == null) {
            userLogin = new UserLogin();
            userLogin.setUserId(userId);
            userLogin.setAccount(account);
            userLogin.setLocked(0);
            userLogin.setLoginFailCount(0);
            userLoginMapper.insert(userLogin);
        }
        return userLogin;
    }

    /**
     * 更新登录失败次数
     */
    @Override
    @Transactional
    public void updateLoginFailCount(String userId, Integer count) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return;
        }
        
        UserLogin userLogin = getOrCreateUserLogin(userId, user.getAccount());
        userLogin.setLoginFailCount(count);
        userLoginMapper.updateById(userLogin);
    }

    /**
     * 锁定用户
     */
    @Override
    @Transactional
    public void lockUser(String userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return;
        }
        
        UserLogin userLogin = getOrCreateUserLogin(userId, user.getAccount());
        userLogin.setLocked(1);
        userLogin.setLockTime(LocalDateTime.now());
        userLoginMapper.updateById(userLogin);
    }

    /**
     * 解锁用户并重置失败次数
     */
    @Override
    @Transactional
    public void unlockUser(String userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return;
        }
        
        UserLogin userLogin = getOrCreateUserLogin(userId, user.getAccount());
        userLogin.setLocked(0);
        userLogin.setLoginFailCount(0);
        userLogin.setLockTime(null);
        userLoginMapper.updateById(userLogin);
    }

    /**
     * 更新最后登录时间
     */
    @Override
    @Transactional
    public void updateLastLoginTime(String userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return;
        }
        
        UserLogin userLogin = getOrCreateUserLogin(userId, user.getAccount());
        userLogin.setLastLoginTime(LocalDateTime.now());
        userLogin.setLoginFailCount(0);
        userLoginMapper.updateById(userLogin);
    }

    /**
     * 获取用户登录信息
     */
    @Override
    public UserLogin getUserLogin(String userId) {
        return userLoginMapper.selectByUserId(userId);
    }

    /**
     * 验证密码
     * 支持PBKDF2和BCrypt两种格式（向后兼容）
     */
    @Override
    public boolean validatePassword(String rawPassword, String encodedPassword) {
        if (encodedPassword == null || encodedPassword.isEmpty()) {
            return false;
        }
        // 去除可能的空格和换行符
        encodedPassword = encodedPassword.trim();
        // PasswordUtil.matches方法内部已经处理了BCrypt和PBKDF2两种格式
        return PasswordUtil.matches(rawPassword, encodedPassword);
    }

    /**
     * 修改密码
     */
    @Override
    @Transactional
    public boolean changePassword(String userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }

        // 验证原密码
        if (!validatePassword(oldPassword, user.getPassword())) {
            return false;
        }

        // 加密新密码
        String encodedNewPassword = PasswordUtil.encode(newPassword);
        user.setPassword(encodedNewPassword);
        
        // 更新密码
        return userMapper.updateById(user) > 0;
    }
}

