package com.hrp.auth.mapper;

import com.hrp.common.entity.Code;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统字典数据访问接口
 */
public interface CodeMapper {
    /**
     * 根据ID查询字典
     */
    Code selectById(@Param("id") String id);

    /**
     * 根据code_name查询字典
     */
    Code selectByCodeName(@Param("codeName") String codeName);

    /**
     * 根据类型查询字典列表
     * @param isStop 停用状态：0-启用，1-停用，null-仅启用
     */
    List<Code> selectByType(@Param("codeType") String codeType, @Param("isStop") Long isStop);

    /**
     * 查询所有字典
     * @param isStop 停用状态：0-启用，1-停用，null-仅启用
     */
    List<Code> selectAll(@Param("isStop") Long isStop);

    /**
     * 插入字典
     */
    int insert(Code code);

    /**
     * 更新字典
     */
    int updateById(Code code);

    /**
     * 删除字典（物理删除）
     */
    int deleteById(@Param("id") String id);

    /**
     * 分页查询所有字典
     * @param isStop 停用状态：0-启用，1-停用，null-仅启用
     */
    List<Code> selectAllPage(@Param("isStop") Long isStop, @Param("offset") Long offset, @Param("size") Long size);

    /**
     * 分页查询字典 - 按类型
     * @param isStop 停用状态：0-启用，1-停用，null-仅启用
     */
    List<Code> selectByTypePage(@Param("codeType") String codeType, @Param("isStop") Long isStop, @Param("offset") Long offset, @Param("size") Long size);

    /**
     * 统计所有字典数量
     * @param isStop 停用状态：0-启用，1-停用，null-仅启用
     */
    Long countAll(@Param("isStop") Long isStop);

    /**
     * 统计指定类型的字典数量
     * @param isStop 停用状态：0-启用，1-停用，null-仅启用
     */
    Long countByType(@Param("codeType") String codeType, @Param("isStop") Long isStop);
}













