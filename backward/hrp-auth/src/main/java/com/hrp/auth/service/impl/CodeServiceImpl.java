package com.hrp.auth.service.impl;

import com.hrp.auth.mapper.CodeMapper;
import com.hrp.auth.service.CodeService;
import com.hrp.common.entity.Code;
import com.hrp.common.entity.PageResult;
import com.hrp.common.util.UuidUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
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
    public Code getByCodeName(String codeName) {
        return codeMapper.selectByCodeName(codeName);
    }

    @Override
    public List<Code> getByType(String codeType, Long isStop) {
        return codeMapper.selectByType(codeType, isStop);
    }

    @Override
    public List<Code> getAll(Long isStop) {
        return codeMapper.selectAll(isStop);
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
        // 如果createUser为空，设置默认值
        if (code.getCreateUser() == null || code.getCreateUser().isEmpty()) {
            code.setCreateUser("SYSTEM");
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
            // 如果createUser为空，设置默认值
            if (code.getCreateUser() == null || code.getCreateUser().isEmpty()) {
                code.setCreateUser("SYSTEM");
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
        return codeMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional
    public boolean toggleStatus(String id) {
        Code code = codeMapper.selectById(id);
        if (code == null) {
            return false;
        }
        Long current = code.getIsStop() == null ? 0L : code.getIsStop();
        Long target = (current != null && current == 1L) ? 0L : 1L;
        
        Code update = new Code();
        update.setId(id);
        update.setIsStop(target);
        update.setUpdateTime(LocalDateTime.now());
        
        return codeMapper.updateById(update) > 0;
    }

    @Override
    public PageResult<Code> getAllPage(Long isStop, Long page, Long size) {
        if (page == null || page < 1) {
            page = 1L;
        }
        if (size == null || size < 1) {
            size = 10L;
        }
        Long offset = (page - 1) * size;
        List<Code> list = codeMapper.selectAllPage(isStop, offset, size);
        Long total = codeMapper.countAll(isStop);
        return new PageResult<>(list, total, size, page);
    }

    @Override
    public PageResult<Code> getByTypePage(String codeType, Long isStop, Long page, Long size) {
        if (page == null || page < 1) {
            page = 1L;
        }
        if (size == null || size < 1) {
            size = 10L;
        }
        Long offset = (page - 1) * size;
        List<Code> list = codeMapper.selectByTypePage(codeType, isStop, offset, size);
        Long total = codeMapper.countByType(codeType, isStop);
        return new PageResult<>(list, total, size, page);
    }
}

