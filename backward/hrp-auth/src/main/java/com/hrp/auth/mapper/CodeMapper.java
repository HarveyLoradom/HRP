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
     * 根据类型查询字典列表
     */
    List<Code> selectByType(@Param("codeType") String codeType);

    /**
     * 查询所有字典
     */
    List<Code> selectAll();

    /**
     * 插入字典
     */
    int insert(Code code);

    /**
     * 更新字典
     */
    int updateById(Code code);

    /**
     * 删除字典（逻辑删除）
     */
    int deleteById(@Param("id") String id);

    /**
     * 分页查询所有字典
     */
    List<Code> selectAllPage(@Param("offset") Long offset, @Param("size") Long size);

    /**
     * 分页查询字典 - 按类型
     */
    List<Code> selectByTypePage(@Param("codeType") String codeType, @Param("offset") Long offset, @Param("size") Long size);

    /**
     * 统计所有字典数量
     */
    Long countAll();

    /**
     * 统计指定类型的字典数量
     */
    Long countByType(@Param("codeType") String codeType);
}








