package com.hrp.user.service.impl;

import com.hrp.user.mapper.CodeMapper;
import com.hrp.user.service.CodeService;
import com.hrp.common.entity.Code;
import com.hrp.common.util.UuidUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统字典服务实现类
 */
@Service
public class CodeServiceImpl implements CodeService {

    @Autowired
    private CodeMapper codeMapper;

    @Override
    public Code getById(String id) {
        return codeMapper.selectById(id);
    }

    @Override
    public List<Code> getByType(String codeType) {
        return codeMapper.selectByType(codeType);
    }

    @Override
    public List<Code> getAll() {
        return codeMapper.selectAll();
    }

    @Override
    @Transactional
    public boolean save(Code code) {
        // 如果ID为空，自动生成UUID
        if (code.getId() == null || code.getId().isEmpty()) {
            code.setId(UuidUtil.generateUuid());
        }
        if (code.getIsStop() == null) {
            code.setIsStop(0L);
        }
        if (code.getCreateTime() == null) {
            code.setCreateTime(LocalDateTime.now());
        }
        return codeMapper.insert(code) > 0;
    }

    @Override
    @Transactional
    public boolean update(Code code) {
        code.setUpdateTime(LocalDateTime.now());
        return codeMapper.updateById(code) > 0;
    }

    @Override
    @Transactional
    public boolean delete(String id) {
        Code code = new Code();
        code.setId(id);
        code.setIsStop(1L);
        return codeMapper.updateById(code) > 0;
    }
}
