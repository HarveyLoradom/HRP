package com.hrp.user.service;

import com.hrp.common.entity.Code;

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
     * 更新字典
     */
    boolean update(Code code);

    /**
     * 删除字典（逻辑删除）
     */
    boolean delete(String id);
}
