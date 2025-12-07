package com.hrp.asset.mapper;

import com.hrp.common.entity.FixedAsset;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FixedAssetMapper {
    FixedAsset selectById(@Param("id") Long id);
    FixedAsset selectByAssetNo(@Param("assetNo") String assetNo);
    List<FixedAsset> selectByStatus(@Param("status") String status);
    List<FixedAsset> selectAll();
    int insert(FixedAsset fixedAsset);
    int updateById(FixedAsset fixedAsset);
    int deleteById(@Param("id") Long id);
}

