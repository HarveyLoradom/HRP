package com.hrp.auth.mapper.impl;

import com.hrp.auth.mapper.PositionMapper;
import com.hrp.common.entity.Position;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 岗位数据访问实现类
 */
@Repository
public class PositionMapperImpl implements PositionMapper {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    private static final String NAMESPACE = "com.hrp.auth.mapper.PositionMapper";

    @Override
    public Position selectById(Long positionId) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", positionId);
    }

    @Override
    public Position selectByCode(String positionCode) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectByCode", positionCode);
    }

    @Override
    public List<Position> selectAll() {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAll");
    }

    @Override
    public int insert(Position position) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", position);
    }

    @Override
    public int updateById(Position position) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", position);
    }

    @Override
    public int deleteById(Long positionId) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", positionId);
    }
}

