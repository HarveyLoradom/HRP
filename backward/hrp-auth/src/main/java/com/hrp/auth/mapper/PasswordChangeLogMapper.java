package com.hrp.auth.mapper;

import com.hrp.common.entity.PasswordChangeLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 密码修改日志数据访问接口
 */
public interface PasswordChangeLogMapper {
    /**
     * 根据ID查询
     */
    PasswordChangeLog selectById(@Param("logId") Long logId);
    
    /**
     * 根据用户ID查询最新的日志记录
     */
    PasswordChangeLog selectByUserId(@Param("userId") String userId);
    
    /**
     * 分页查询
     */
    List<PasswordChangeLog> selectPage(Map<String, Object> params);
    
    /**
     * 分页查询总数
     */
    long countPage(Map<String, Object> params);
    
    /**
     * 插入日志
     */
    int insert(PasswordChangeLog log);
    
    /**
     * 根据用户ID更新日志
     */
    int updateByUserId(PasswordChangeLog log);
    
    /**
     * 根据ID删除
     */
    int deleteById(@Param("logId") Long logId);
    
    /**
     * 根据用户ID删除所有密码修改日志
     */
    int deleteByUserId(@Param("userId") String userId);
}

