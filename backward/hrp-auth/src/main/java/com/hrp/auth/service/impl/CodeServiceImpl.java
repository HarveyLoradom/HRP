package com.hrp.auth.service.impl;

import com.hrp.auth.mapper.CodeMapper;
import com.hrp.auth.service.CodeService;
import com.hrp.common.entity.Code;
import com.hrp.common.entity.PageResult;
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
    public boolean saveBatch(List<Code> codeList) {
        if (codeList == null || codeList.isEmpty()) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        for (Code code : codeList) {
            // 如果ID为空，自动生成UUID
            if (code.getId() == null || code.getId().isEmpty()) {
                code.setId(UuidUtil.generateUuid());
            }
            if (code.getIsStop() == null) {
                code.setIsStop(0L);
            }
            if (code.getCreateTime() == null) {
                code.setCreateTime(now);
            }
            int result = codeMapper.insert(code);
            if (result <= 0) {
                return false;
            }
        }
        return true;
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

    @Override
    public PageResult<Code> getAllPage(Long page, Long size) {
        if (page == null || page < 1) {
            page = 1L;
        }
        if (size == null || size < 1) {
            size = 10L;
        }
        Long offset = (page - 1) * size;
        List<Code> records = codeMapper.selectAllPage(offset, size);
        Long total = codeMapper.countAll();
        return PageResult.of(records, total, size, page);
    }

    @Override
    public PageResult<Code> getByTypePage(String codeType, Long page, Long size) {
        if (page == null || page < 1) {
            page = 1L;
        }
        if (size == null || size < 1) {
            size = 10L;
        }
        Long offset = (page - 1) * size;
        List<Code> records = codeMapper.selectByTypePage(codeType, offset, size);
        Long total = codeMapper.countByType(codeType);
        return PageResult.of(records, total, size, page);
    }
}

