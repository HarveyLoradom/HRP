package com.hrp.auth.mapper.impl;

import com.hrp.auth.mapper.UserMenuMapper;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户菜单关联数据访问实现类
 */
@Repository
public class UserMenuMapperImpl implements UserMenuMapper {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    private static final String NAMESPACE = "com.hrp.auth.mapper.UserMenuMapper";

    @Override
    public List<Long> selectMenuIdsByUserId(String userId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectMenuIdsByUserId", userId);
    }

    @Override
    public int deleteByUserId(String userId) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteByUserId", userId);
    }

    @Override
    public int batchInsert(String userId, List<Long> menuIds) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("menuIds", menuIds);
        return sqlSessionTemplate.insert(NAMESPACE + ".batchInsert", params);
    }
}

