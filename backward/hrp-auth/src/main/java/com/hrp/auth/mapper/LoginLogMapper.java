package com.hrp.auth.mapper;

import com.hrp.common.entity.LoginLog;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * 登录日志数据访问接口
 */
public interface LoginLogMapper {
    /**
     * 插入登录日志
     */
    int insert(LoginLog loginLog);
    
    /**
     * 删除指定时间之前的登录日志
     * @param beforeTime 删除此时间之前的记录
     * @return 删除的记录数
     */
    int deleteBeforeTime(@Param("beforeTime") LocalDateTime beforeTime);
    
    /**
     * 统计登录日志总数
     * @return 总记录数
     */
    long count();
}

