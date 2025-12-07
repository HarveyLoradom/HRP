package com.hrp.user.mapper;

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
     * 删除字典
     */
    int deleteById(@Param("id") String id);
}
