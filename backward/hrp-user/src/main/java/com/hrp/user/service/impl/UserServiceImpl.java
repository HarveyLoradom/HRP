package com.hrp.user.service.impl;

import com.hrp.auth.mapper.UserMapper;
import com.hrp.auth.mapper.UserLoginMapper;
import com.hrp.common.entity.Code;
import com.hrp.common.entity.User;
import com.hrp.common.entity.UserLogin;
import com.hrp.common.util.PasswordUtil;
import com.hrp.common.util.UuidUtil;
import com.hrp.user.service.CodeService;
import com.hrp.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户管理服务实现类
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CodeService codeService;

    @Override
    public User getById(String id) {
        return userMapper.selectById(id);
    }

    @Override
    public User getByAccount(String account) {
        return userMapper.selectByAccount(account);
    }

    @Override
    public List<User> getAll() {
        return userMapper.selectAll();
    }

    @Override
    @Transactional
    public boolean save(User user) {
        if (user.getId() == null || user.getId().isEmpty()) {
            user.setId(UuidUtil.generateUuid());
        }
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(PasswordUtil.encode(user.getPassword()));
        }
        if (user.getIsStop() == null) {
            user.setIsStop(0L);
        }
        return userMapper.insert(user) > 0;
    }

    @Override
    @Transactional
    public boolean update(User user) {
        if (user.getId() == null || user.getId().isEmpty()) {
            return false;
        }
        // 如果密码不为空，需要加密
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            // 检查密码是否已经是加密格式（BCrypt格式以$2a$或$2b$开头）
            if (!user.getPassword().startsWith("$2")) {
                user.setPassword(PasswordUtil.encode(user.getPassword()));
            }
        }
        return userMapper.updateById(user) > 0;
    }

    @Override
    @Transactional
    public boolean delete(String id) {
        User user = new User();
        user.setId(id);
        user.setIsStop(1L);
        return userMapper.updateById(user) > 0;
    }

    @Override
    @Transactional
    public boolean toggleStatus(String id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return false;
        }
        user.setIsStop(user.getIsStop() == 0L ? 1L : 0L);
        return userMapper.updateById(user) > 0;
    }

    @Override
    @Transactional
    public boolean resetPassword(String id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return false;
        }
        
        // 从sys_code表获取原始密码参数
        Code resetPasswordCode = codeService.getById("RESET_PASSWORD");
        if (resetPasswordCode == null || resetPasswordCode.getCodeValue() == null) {
            // 如果参数不存在，使用默认密码123456
            user.setPassword(PasswordUtil.encode("123456"));
        } else {
            // 使用配置的原始密码
            String defaultPassword = resetPasswordCode.getCodeValue();
            user.setPassword(PasswordUtil.encode(defaultPassword));
        }
        
        return userMapper.updateById(user) > 0;
    }

    @Override
    @Transactional
    public boolean unlockUser(String id) {
        UserLogin userLogin = userLoginMapper.selectByUserId(id);
        if (userLogin == null) {
            return false;
        }
        userLogin.setLocked(0);
        userLogin.setLoginFailCount(0);
        userLogin.setLockTime(null);
        return userLoginMapper.updateById(userLogin) > 0;
    }
}



