package com.hrp.auth.service;

import com.hrp.common.entity.Code;
import com.hrp.common.entity.PageResult;

import java.util.List;

/**
 * 系统字典服务接口
 */
public interface CodeService {
    /**
     * 根据ID查询字典
     */
    Code getById(String id);

    /**
     * 根据类型查询字典列表
     */
    List<Code> getByType(String codeType);

    /**
     * 查询所有字典
     */
    List<Code> getAll();

    /**
     * 新增字典
     */
    boolean save(Code code);

    /**
     * 批量新增字典
     */
    boolean saveBatch(List<Code> codeList);

    /**
     * 更新字典
     */
    boolean update(Code code);

    /**
     * 删除字典（逻辑删除）
     */
    boolean delete(String id);

    /**
     * 分页查询所有字典
     */
    PageResult<Code> getAllPage(Long page, Long size);

    /**
     * 分页查询字典 - 按类型
     */
    PageResult<Code> getByTypePage(String codeType, Long page, Long size);
}

