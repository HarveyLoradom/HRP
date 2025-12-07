package com.hrp.auth.mapper;

import com.hrp.common.entity.LoginLog;

/**
 * 登录日志数据访问接口
 */
public interface LoginLogMapper {
    /**
     * 插入登录日志
     */
    int insert(LoginLog loginLog);
}

