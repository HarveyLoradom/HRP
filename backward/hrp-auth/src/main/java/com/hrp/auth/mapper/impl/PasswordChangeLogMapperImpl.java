package com.hrp.auth.mapper.impl;

import com.hrp.auth.mapper.PasswordChangeLogMapper;
import com.hrp.common.entity.PasswordChangeLog;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * 密码修改日志数据访问实现类
 */
@Repository
public class PasswordChangeLogMapperImpl implements PasswordChangeLogMapper {

    private static final String NAMESPACE = "com.hrp.auth.mapper.PasswordChangeLogMapper";

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    @Override
    public PasswordChangeLog selectById(Long logId) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", logId);
    }

    @Override
    public PasswordChangeLog selectByUserId(String userId) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectByUserId", userId);
    }

    @Override
    public List<PasswordChangeLog> selectPage(Map<String, Object> params) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectPage", params);
    }

    @Override
    public long countPage(Map<String, Object> params) {
        Long count = sqlSessionTemplate.selectOne(NAMESPACE + ".countPage", params);
        return count != null ? count : 0;
    }

    @Override
    public int insert(PasswordChangeLog log) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", log);
    }

    @Override
    public int updateByUserId(PasswordChangeLog log) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateByUserId", log);
    }

    @Override
    public int deleteById(Long logId) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", logId);
    }
    
    @Override
    public int deleteByUserId(String userId) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteByUserId", userId);
    }
}

