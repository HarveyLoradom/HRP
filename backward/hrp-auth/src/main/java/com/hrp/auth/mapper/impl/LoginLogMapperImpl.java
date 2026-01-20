package com.hrp.auth.mapper.impl;

import com.hrp.auth.mapper.LoginLogMapper;
import com.hrp.common.entity.LoginLog;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * 登录日志数据访问实现类
 */
@Repository
public class LoginLogMapperImpl implements LoginLogMapper {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    private static final String NAMESPACE = "com.hrp.auth.mapper.LoginLogMapper";

    @Override
    public int insert(LoginLog loginLog) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", loginLog);
    }

    @Override
    public int deleteBeforeTime(LocalDateTime beforeTime) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteBeforeTime", beforeTime);
    }

    @Override
    public long count() {
        Long result = sqlSessionTemplate.selectOne(NAMESPACE + ".count");
        return result != null ? result : 0L;
    }
}

