package com.hrp.auth.mapper.impl;

import com.hrp.auth.mapper.PositionUserMapper;
import com.hrp.common.entity.PositionUser;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 岗位人员关联数据访问实现类
 */
@Repository
public class PositionUserMapperImpl implements PositionUserMapper {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    private static final String NAMESPACE = "com.hrp.auth.mapper.PositionUserMapper";

    @Override
    public List<PositionUser> selectByPositionId(Long positionId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByPositionId", positionId);
    }

    @Override
    public List<PositionUser> selectByUserId(String userId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByUserId", userId);
    }

    @Override
    public int insert(PositionUser positionUser) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", positionUser);
    }

    @Override
    public int deleteByPositionId(Long positionId) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteByPositionId", positionId);
    }

    @Override
    public int deleteByUserId(String userId) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteByUserId", userId);
    }

    @Override
    public int deleteByPositionIdAndUserId(Long positionId, String userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("positionId", positionId);
        params.put("userId", userId);
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteByPositionIdAndUserId", params);
    }

    @Override
    public int batchInsert(Long positionId, List<String> userIds) {
        Map<String, Object> params = new HashMap<>();
        params.put("positionId", positionId);
        params.put("userIds", userIds);
        return sqlSessionTemplate.insert(NAMESPACE + ".batchInsert", params);
    }
}

