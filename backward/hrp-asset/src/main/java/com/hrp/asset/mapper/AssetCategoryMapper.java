package com.hrp.asset.mapper;

import com.hrp.common.entity.AssetCategory;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 资产分类数据访问接口
 */
public interface AssetCategoryMapper {
    List<AssetCategory> selectLevel1ByConditions(@Param("status") Integer status, @Param("categoryName") String categoryName);
    List<AssetCategory> selectLevel2ByConditions(@Param("parentId") Long parentId, @Param("status") Integer status, @Param("categoryName") String categoryName);
    List<AssetCategory> selectLevel3ByConditions(@Param("parentId") Long parentId, @Param("level1Id") Long level1Id, @Param("status") Integer status, @Param("categoryName") String categoryName);
    AssetCategory selectById(@Param("id") Long id);
    AssetCategory selectByCode(@Param("code") String code);
    int insert(AssetCategory category);
    int updateById(AssetCategory category);
    int deleteById(@Param("id") Long id);
    /**
     * 查询指定父分类下的最大编码（用于自动生成编码）
     */
    String selectMaxCodeByParentId(@Param("parentId") Long parentId);
    /**
     * 检查分类下是否有子分类
     */
    int countChildrenByParentId(@Param("parentId") Long parentId);
    
    /**
     * 检查分类下是否有未停用的子分类（用于停用检查）
     */
    int countActiveChildrenByParentId(@Param("parentId") Long parentId);
    /**
     * 查询所有一级分类（用于下拉选择）
     */
    List<AssetCategory> selectAllLevel1(@Param("status") Integer status);
    /**
     * 查询指定一级分类下的所有二级分类（用于下拉选择）
     */
    List<AssetCategory> selectAllLevel2ByParentId(@Param("parentId") Long parentId, @Param("status") Integer status);
    /**
     * 查询指定二级分类下的所有三级分类（用于下拉选择）
     */
    List<AssetCategory> selectAllLevel3ByParentId(@Param("parentId") Long parentId, @Param("status") Integer status);
    
    /**
     * 根据名称和级别精确查询分类（用于导入时根据名称查找ID）
     */
    AssetCategory selectByNameAndLevel(@Param("categoryName") String categoryName, @Param("level") Integer level, @Param("parentId") Long parentId);
}

