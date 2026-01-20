package com.hrp.menu.mapper.impl;

import com.hrp.menu.mapper.MenuMapper;
import com.hrp.common.entity.Menu;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 菜单数据访问实现类
 */
@Repository
public class MenuMapperImpl implements MenuMapper {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    private static final String NAMESPACE = "com.hrp.menu.mapper.MenuMapper";

    @Override
    public Menu selectById(Long id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }

    @Override
    public List<Menu> selectAll() {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAll");
    }

    @Override
    public List<Menu> selectMenusByUserId(String userId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectMenusByUserId", userId);
    }

    @Override
    public int insert(Menu menu) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", menu);
    }

    @Override
    public int updateById(Menu menu) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", menu);
    }

    @Override
    public int deleteById(Long id) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", id);
    }
}

