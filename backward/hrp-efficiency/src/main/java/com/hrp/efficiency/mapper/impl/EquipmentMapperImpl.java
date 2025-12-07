package com.hrp.efficiency.mapper.impl;

import com.hrp.efficiency.mapper.EquipmentMapper;
import com.hrp.common.entity.Equipment;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EquipmentMapperImpl implements EquipmentMapper {
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    private static final String NAMESPACE = "com.hrp.efficiency.mapper.EquipmentMapper";

    @Override
    public Equipment selectById(Long id) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", id);
    }

    @Override
    public Equipment selectByEquipmentCode(String equipmentCode) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectByEquipmentCode", equipmentCode);
    }

    @Override
    public List<Equipment> selectByStatus(String status) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByStatus", status);
    }

    @Override
    public List<Equipment> selectAll() {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectAll");
    }

    @Override
    public int insert(Equipment equipment) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", equipment);
    }

    @Override
    public int updateById(Equipment equipment) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", equipment);
    }

    @Override
    public int deleteById(Long id) {
        return sqlSessionTemplate.delete(NAMESPACE + ".deleteById", id);
    }
}

