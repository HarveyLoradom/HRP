package com.hrp.user.mapper.impl;

import com.hrp.user.mapper.UserMenuMapper;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户菜单关联数据访问实现类
 */
@Repository
public class UserMenuMapperImpl implements UserMenuMapper {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    private static final String NAMESPACE = "com.hrp.user.mapper.UserMenuMapper";

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
        if (menuIds == null || menuIds.isEmpty()) {
            return 0;
        }
        // 构建批量插入的参数
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("userId", userId);
        params.put("menuIds", menuIds);
        return sqlSessionTemplate.insert(NAMESPACE + ".batchInsert", params);
    }
}
