package com.hrp.auth.mapper.impl;

import com.hrp.auth.mapper.AddSignRecordMapper;
import com.hrp.common.entity.AddSignRecord;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 加签记录数据访问实现类
 */
@Repository
public class AddSignRecordMapperImpl implements AddSignRecordMapper {

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    private static final String NAMESPACE = "com.hrp.auth.mapper.AddSignRecordMapper";

    @Override
    public AddSignRecord selectById(Long addsignId) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectById", addsignId);
    }

    @Override
    public List<AddSignRecord> selectByParentTaskId(Long parentTaskId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByParentTaskId", parentTaskId);
    }

    @Override
    public List<AddSignRecord> selectByProcessInstanceId(Long processInstanceId) {
        return sqlSessionTemplate.selectList(NAMESPACE + ".selectByProcessInstanceId", processInstanceId);
    }

    @Override
    public AddSignRecord selectByAddsignTaskId(Long addsignTaskId) {
        return sqlSessionTemplate.selectOne(NAMESPACE + ".selectByAddsignTaskId", addsignTaskId);
    }

    @Override
    public int insert(AddSignRecord record) {
        return sqlSessionTemplate.insert(NAMESPACE + ".insert", record);
    }

    @Override
    public int updateById(AddSignRecord record) {
        return sqlSessionTemplate.update(NAMESPACE + ".updateById", record);
    }
}

