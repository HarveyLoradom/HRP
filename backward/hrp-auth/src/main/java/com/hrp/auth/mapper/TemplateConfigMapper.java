package com.hrp.auth.mapper;

import com.hrp.common.entity.TemplateConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 模板设置数据访问接口
 */
public interface TemplateConfigMapper {
    /**
     * 根据ID查询模板设置
     */
    TemplateConfig selectById(@Param("configId") Long configId);

    /**
     * 根据业务类型和业务类型值查询模板设置
     */
    TemplateConfig selectByBusinessType(@Param("businessType") String businessType, 
                                        @Param("businessTypeValue") String businessTypeValue);

    /**
     * 根据业务类型查询模板设置列表
     */
    List<TemplateConfig> selectByBusinessTypeOnly(@Param("businessType") String businessType);

    /**
     * 查询所有模板设置
     */
    List<TemplateConfig> selectAll();

    /**
     * 分页查询所有模板设置
     */
    List<TemplateConfig> selectAllPage(@Param("offset") Long offset, @Param("size") Long size);

    /**
     * 统计总数
     */
    Long countAll();

    /**
     * 插入模板设置
     */
    int insert(TemplateConfig config);

    /**
     * 更新模板设置
     */
    int updateById(TemplateConfig config);

    /**
     * 删除模板设置
     */
    int deleteById(@Param("configId") Long configId);
}

