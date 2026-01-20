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
     * 根据code_name查询字典
     */
    Code getByCodeName(String codeName);

    /**
     * 根据类型查询字典列表
     * @param isStop 停用状态：0-启用，1-停用，null-仅启用
     */
    List<Code> getByType(String codeType, Long isStop);

    /**
     * 查询所有字典
     * @param isStop 停用状态：0-启用，1-停用，null-仅启用
     */
    List<Code> getAll(Long isStop);

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
     * 删除字典（物理删除）
     */
    boolean delete(String id);

    /**
     * 停用/启用字典
     */
    boolean toggleStatus(String id);

    /**
     * 分页查询所有字典
     * @param isStop 停用状态：0-启用，1-停用，null-仅启用
     */
    PageResult<Code> getAllPage(Long isStop, Long page, Long size);

    /**
     * 分页查询字典 - 按类型
     * @param isStop 停用状态：0-启用，1-停用，null-仅启用
     */
    PageResult<Code> getByTypePage(String codeType, Long isStop, Long page, Long size);
}

